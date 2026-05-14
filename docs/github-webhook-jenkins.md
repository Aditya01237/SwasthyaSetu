# GitHub Webhook For Jenkins

The final project rubric asks for incremental repository updates to trigger automated Jenkins processes. This project enables that through the Jenkins `githubPush()` trigger plus a GitHub webhook.

## Jenkins Requirements

Install these Jenkins plugins:

- Git
- GitHub
- Pipeline
- Credentials Binding
- SSH Agent
- JUnit

## Jenkins Job Setup

1. Open Jenkins.
2. Open the `SwasthyaSetu` Pipeline job.
3. Select Configure.
4. Under Pipeline:
   - Definition: Pipeline script from SCM.
   - SCM: Git.
   - Repository URL: `https://github.com/Aditya01237/SwasthyaSetu.git`
   - Branch Specifier: `*/aditya-branch`
   - Script Path: `Jenkinsfile`
5. Under **Build Triggers**, enable **GitHub hook trigger for GITScm polling** (CSE 816).
6. Save the job.

The root `Jenkinsfile` declares `triggers { githubPush() }` so pushes are tied to the pipeline when the GitHub plugin is installed and the webhook below is configured.

## GitHub Webhook Setup

1. Open GitHub repository: `https://github.com/Aditya01237/SwasthyaSetu`
2. Go to Settings -> Webhooks -> Add webhook.
3. Payload URL:
   - Local Jenkins with ngrok/cloudflared: `https://<public-tunnel-url>/github-webhook/`
   - Public Jenkins server: `http://<jenkins-host>:8080/github-webhook/`
4. Content type: `application/json`.
5. Secret: leave empty unless Jenkins is configured to validate a shared secret.
6. Select: Just the push event.
7. Check Active.
8. Click Add webhook.

## If Jenkins Is Running Only On Your Laptop

GitHub cannot directly reach `localhost`. Use one of these:

```bash
ngrok http 8080
```

or:

```bash
cloudflared tunnel --url http://localhost:8080
```

Use the public HTTPS URL from the tunnel as the webhook Payload URL, ending with `/github-webhook/`.

## Demo Proof

For evaluation, show these:

- GitHub webhook page showing a recent successful delivery.
- Jenkins build history showing a build started by GitHub push.
- Jenkins console output showing checkout, tests, Docker build/publish, and deploy stages.
