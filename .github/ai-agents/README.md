# AI Agents for Project Management

This directory contains specialized AI agents and prompts to ensure the AI Scientist Ecosystem project is developed, maintained, and deployed with zero security vulnerabilities and maximum quality.

## 🤖 Agent Structure

Each agent has:
- **Role Definition**: Clear responsibilities and expertise
- **Quality Checklist**: Standards and requirements
- **Validation Rules**: Automated checks
- **Security Guidelines**: Security-first approach
- **Best Practices**: Industry standards

## 📁 Agent Directory

| Agent | File | Purpose |
|-------|------|---------|
| **Architect Agent** | `architect-agent.md` | System design, architecture decisions |
| **Security Agent** | `security-agent.md` | Security audit, vulnerability scanning |
| **DevOps Agent** | `devops-agent.md` | Infrastructure, deployment, monitoring |
| **Backend Agent** | `backend-agent.md` | Spring Boot microservices development |
| **Frontend Agent** | `frontend-agent.md` | Next.js, React Native development |
| **AI/ML Agent** | `ai-ml-agent.md` | AI agents, ML models, research |
| **Database Agent** | `database-agent.md` | Schema design, migrations, optimization |
| **Testing Agent** | `testing-agent.md` | Unit, integration, E2E testing |
| **Documentation Agent** | `documentation-agent.md` | Technical docs, API docs, guides |
| **Code Review Agent** | `code-review-agent.md` | Code quality, best practices |

## 🚀 How to Use

### 1. Starting a New Feature
```
Use: Architect Agent → Backend/Frontend Agent → Testing Agent → Code Review Agent
```

### 2. Security Audit
```
Use: Security Agent → DevOps Agent → Code Review Agent
```

### 3. Deployment
```
Use: DevOps Agent → Testing Agent → Security Agent
```

### 4. Bug Fix
```
Use: Backend/Frontend Agent → Testing Agent → Code Review Agent
```

## 🎯 Quality Gates

Every change must pass through:
1. ✅ Architecture review
2. ✅ Security scan
3. ✅ Code quality check
4. ✅ Test coverage > 80%
5. ✅ Documentation updated
6. ✅ Performance benchmarks
7. ✅ Security compliance

## 📊 Workflow Integration

Agents are integrated with:
- GitHub Actions (CI/CD)
- SonarQube (Code Quality)
- OWASP Dependency Check (Security)
- Trivy (Container Scanning)
- JUnit/Jest (Testing)

## 🔒 Security First

All agents enforce:
- Zero trust architecture
- Least privilege principle
- Defense in depth
- Secure by default
- Privacy by design
