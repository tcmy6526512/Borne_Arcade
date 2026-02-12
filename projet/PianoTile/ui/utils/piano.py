import pygame, random
from ui.utils.note import Note

class Piano:
    def __init__(self, gameview):
        self.__gameView = gameview
        self.__filepath = "./assets/music/" + self.__gameView.getWindowManager().getMusicSelect().lower().replace('play musique ', '').replace(' ', '').replace("'", '').replace(',', '') + ".mp3"
        self.__difficulty = 1
        self.__notes = self.generate_notes()

    def getNotes(self):
        return self.__notes

    def increaseDifficulty(self):
        self.__difficulty += 1

    def play(self):
        pygame.mixer.init()
        pygame.mixer.music.load(self.__filepath)
        pygame.mixer.music.play()

    def pause(self):
        pygame.mixer.music.pause()

    def generate_notes(self):
        print("Génération des notes à partir du fichier :", self.__filepath)
        notes = []

        # Fallback sans librosa: on génère des notes à intervalle régulier.
        # C'est moins précis qu'une détection de beat, mais le jeu reste jouable
        # sur les environnements Linux où librosa/numba est difficile à installer.
        temps = 0.8
        intervalle = 0.45
        duree_estimee = 120.0

        while temps < duree_estimee:
            nb_notes = min(self.__difficulty, random.randint(1, 2))
            for _ in range(nb_notes):
                position = random.choice(["left", "middle", "right", "top"])
                note = Note(gameview=self.__gameView, position=position, timestamp=temps)
                notes.append(note)
            temps += intervalle

        print(f"{len(notes)} notes générées.")
        return notes

    def getCurrentTime(self):
        return pygame.mixer.music.get_pos() / 1000.0
