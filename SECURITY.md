# Security Policy

## Supported versions

VioletCore is currently pre-beta. Security fixes target the latest `main` branch and the latest release tag.

## Sensitive files

Do not commit private keys, access tokens, production server worlds, private configuration files, or paid/plugin license files.

Use private infrastructure for production secrets and credentials.

## If a secret leaks

1. Revoke/delete the secret immediately.
2. Generate a new secret.
3. Check public repository history and release assets.
4. Rebuild and redeploy with the new secret.

## Engine Plugin safety

Engine Plugins are powerful server-side modules. Install only jars you trust.

Before publishing an Engine Plugin, verify:

- `engine-plugin.yml` has the correct `target-version`.
- `modifies:` accurately declares touched systems.
- The plugin does not modify chunk storage, player inventory transactions, or networking unless explicitly designed and tested for it.

## Reporting vulnerabilities

Open a GitHub Security Advisory if available, or contact the repository owner privately.

Please include:

- Affected version.
- Steps to reproduce.
- Expected impact.
- Suggested fix if known.
