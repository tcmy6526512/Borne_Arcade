from __future__ import annotations

import json
import socket
import urllib.request
from dataclasses import dataclass
from typing import Any, Dict, Mapping, Optional


class OllamaError(RuntimeError):
    pass


class OllamaConnectionError(OllamaError):
    pass


class OllamaResponseError(OllamaError):
    pass


@dataclass(frozen=True)
class OllamaGenerateResult:
    response: str
    model: Optional[str] = None
    done: Optional[bool] = None


class OllamaWrapper:
    """
    Wrapper IUT minimal utilisé par l'automatisation docs.
    Par défaut il pointe vers le serveur Ollama réseau de l'IUT.
    """

    def __init__(self, base_url: str = "http://10.22.28.190:11434", timeout_s: float = 60.0) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout_s = timeout_s

    def _parse_host_port(self) -> tuple[str, int]:
        url = self._base_url
        if url.startswith("http://"):
            url = url[len("http://") :]
        elif url.startswith("https://"):
            url = url[len("https://") :]
        if ":" in url:
            host, port = url.split(":", 1)
            return host, int(port)
        return url, 11434

    def is_server_running(self) -> bool:
        host, port = self._parse_host_port()
        try:
            with socket.create_connection((host, port), timeout=0.8):
                return True
        except OSError:
            return False

    def get_version(self) -> str:
        payload = self._http_request_json("GET", "/api/version", body=None)
        version = payload.get("version")
        if not isinstance(version, str):
            raise OllamaResponseError(f"Réponse /api/version inattendue: {payload!r}")
        return version

    def generate_text(
        self,
        *,
        model: str,
        prompt: str,
        system: Optional[str] = None,
        options: Optional[Mapping[str, Any]] = None,
    ) -> OllamaGenerateResult:
        body: Dict[str, Any] = {
            "model": model,
            "prompt": prompt,
            "stream": False,
        }
        if system is not None:
            body["system"] = system
        if options is not None:
            body["options"] = dict(options)

        payload = self._http_request_json("POST", "/api/generate", body=body)
        response = payload.get("response")
        if not isinstance(response, str):
            raise OllamaResponseError(f"Réponse /api/generate inattendue: {payload!r}")

        return OllamaGenerateResult(
            response=response,
            model=payload.get("model") if isinstance(payload.get("model"), str) else None,
            done=payload.get("done") if isinstance(payload.get("done"), bool) else None,
        )

    def _http_request_json(self, method: str, path: str, *, body: Optional[Mapping[str, Any]]) -> Dict[str, Any]:
        data = None if body is None else json.dumps(body).encode("utf-8")
        req = urllib.request.Request(
            url=f"{self._base_url}/{path.lstrip('/')}",
            data=data,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
            method=method,
        )
        try:
            with urllib.request.urlopen(req, timeout=self._timeout_s) as resp:
                raw = resp.read().decode("utf-8", errors="replace")
        except Exception as exc:
            raise OllamaConnectionError(f"Impossible de joindre Ollama ({self._base_url}): {exc}") from exc

        try:
            payload = json.loads(raw)
        except json.JSONDecodeError as exc:
            raise OllamaResponseError(f"Réponse non JSON: {raw[:200]!r}") from exc

        if not isinstance(payload, dict):
            raise OllamaResponseError(f"JSON inattendu: {payload!r}")
        return payload
