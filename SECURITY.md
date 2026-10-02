# Security Policy

Do not disclose suspected vulnerabilities in public issues. Use the repository's private security reporting mechanism once the project repository is published.

Backend JVMs are treated as untrusted boundaries relative to Core. Managed deployments should pin artifact hashes, keep backend networking private, and protect secrets such as authentication keys.

The repository does not yet claim a complete sandbox implementation; host/container isolation remains required for production deployment.
