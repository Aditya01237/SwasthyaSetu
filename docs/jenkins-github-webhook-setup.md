# Jenkins GitHub Webhook Setup for SwasthyaSetu

## Why This is Needed

The CSE 816 DevOps evaluation expects Jenkins to automatically start the CI/CD pipeline when code is pushed to GitHub.

For SwasthyaSetu, the Jenkinsfile already contains:

```groovy
triggers {
    githubPush()
}
```

This means the Jenkins pipeline is ready to respond to GitHub webhook events.

## Jenkins Job Configuration

In Jenkins job configuration, enable:

```text
Build Triggers
GitHub hook trigger for GITScm polling
```

This connects GitHub push events with the Jenkins pipeline.

## GitHub Repository Webhook

In the GitHub repository, add a webhook pointing to Jenkins:

```text
Payload URL:
http://<jenkins-server-url>/github-webhook/
```

For local Jenkins using ngrok or a public tunnel, it may look like:

```text
https://<ngrok-url>/github-webhook/
```

Use:

```text
Content type: application/json
Event: Just the push event
Active: checked
```

## Expected Flow

```text
Developer pushes code to GitHub
        ↓
GitHub sends webhook event
        ↓
Jenkins receives event at /github-webhook/
        ↓
Jenkins checks Git SCM changes
        ↓
Pipeline starts automatically
        ↓
Tests, Docker build, Docker push, and deployment run
```

## Demo Explanation

During evaluation, explain it like this:

```text
The Jenkinsfile contains githubPush(), and the Jenkins job is configured with GitHub hook trigger for GITScm polling.
Whenever code is pushed to GitHub, GitHub sends a webhook to Jenkins, and Jenkins automatically starts the pipeline.
```

## Important Note

The webhook trigger is partly code-based and partly Jenkins UI-based.

Code side:

```groovy
triggers {
    githubPush()
}
```

Jenkins UI side:

```text
GitHub hook trigger for GITScm polling
```

GitHub side:

```text
Repository Settings → Webhooks → Jenkins /github-webhook/ URL
```

All three together complete the GitHub-to-Jenkins automation flow.
