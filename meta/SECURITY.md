# Security Policy

## Supported Branches
| Branch | Status |
| ------ | ------ |
| main   | Supported |

Feature branches are short-lived and not individually supported.

## Reporting a Vulnerability
Please email: security@example.org (replace with real alias) with details:
- Affected module/repository
- Description of issue
- Steps to reproduce
- Potential impact

Do NOT open a public issue for sensitive vulnerabilities.

We aim to respond within 72 hours and provide a remediation plan or timeline.

## Handling Secrets
Use environment variables or secret managers (e.g., GitHub Actions secrets, Vault, AWS/GCP secret manager). Never commit secrets.

## Dependency Updates
Automated via Dependabot (to be enabled). High severity CVEs prioritized.

## Secure Development Practices (Initial)
- Mandatory code review
- Lint + static analysis in CI
- Avoid executing untrusted code

## Future Enhancements
- SAST scanning integration
- SBOM generation
- Regular dependency audit reports
