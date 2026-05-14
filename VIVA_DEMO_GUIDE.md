# SwasthyaSetu DevOps Viva Demo Guide 🚀

This document is your step-by-step "cheat sheet" to flawlessly present your DevOps project to your examiner and score the full 25/25 marks.

## ⚠️ Pre-Viva Checklist (Do this 30 minutes before your Viva)
Before sharing your screen, get your environment into a clean, running state.

1. **Stop unused containers** to save RAM:
   ```bash
   docker compose down
   docker stop $(docker ps -a -q)
   ```
2. **Start Minikube with 8GB RAM**:
   ```bash
   minikube start --cpus=6 --memory=8192
   ```
3. **Start Ngrok** (So GitHub can reach your Jenkins):
   ```bash
   ngrok http --domain=henriette-tamest-armanda.ngrok-free.dev 8085 > /dev/null 2>&1 &
   ```
4. **Deploy your K8s Cluster**:
   ```bash
   # Wait for the script to finish and all pods to say READY
   sh scripts/k8s-phased-start.sh
   ```
5. **Open the Minikube Tunnel** (Run this in a NEW, separate terminal window and leave it running):
   ```bash
   minikube tunnel
   ```
6. **Open your tabs in Chrome**:
   - Tab 1: Jenkins Dashboard (`http://localhost:8085`)
   - Tab 2: GitHub Repository (SwasthyaSetu)
   - Tab 3: SwasthyaSetu Patient Portal (`http://localhost/patient/`)
   - Tab 4: SwasthyaSetu Doctor Portal (`http://localhost/doctor/`)

---

## 🎬 Viva Script (What to show and say)

### Step 1: The CI/CD Pipeline (Mandatory: 20 Marks)
**Goal:** Show that code changes trigger Jenkins to automatically build and deploy.

1. **Say:** *"My project uses a fully automated CI/CD pipeline built with Jenkins. When a developer pushes code to GitHub, a webhook automatically triggers the pipeline."*
2. **Action:** Open your GitHub repo and edit a visible file (like the frontend `index.html` title or a simple `README.md`). Commit the change directly to the `aditya-branch`.
3. **Action:** Immediately switch to your Jenkins tab (`http://localhost:8085`).
4. **Say:** *"As you can see, the GitHub webhook instantly triggered Jenkins. The pipeline is now running through multiple stages."*
5. **Action:** Click into the running build and show the **Pipeline Flow** (the green stages). Point out the parallel testing, frontend builds, Docker image pushing (`docker.io/adityapareek01/swasthya-setu-...`), and finally the K8s deployment.

### Step 2: The Application (Seamless Updates)
**Goal:** Prove the application works and the new changes are live.

1. **Action:** Open the `http://localhost/patient/` tab. Refresh the page.
2. **Say:** *"The pipeline has completed, and our Kubernetes cluster has automatically pulled the new Docker images. By refreshing the page, the application is seamlessly updated without manual intervention."*

### Step 3: Advanced Features - Vault & Ansible (Extra: 3 Marks)
**Goal:** Prove modularity and secure storage.

1. **Action:** Open VS Code and show the `ansible/roles/` folder.
2. **Say:** *"For configuration management, I used Ansible. To ensure modular design, I split my playbooks into roles like `app`, `common`, and `docker`."*
3. **Action:** Show the `scripts/ci/setup-vault.sh` file.
4. **Say:** *"For secure storage of sensitive credentials, I integrated HashiCorp Vault into the CI pipeline setup."*

### Step 4: Advanced Features - Kubernetes Auto-Scaling (Extra: 3 Marks)
**Goal:** Prove High Availability via HPA (Horizontal Pod Autoscaling).

1. **Action:** Open your terminal and run:
   ```bash
   kubectl get hpa -n swasthya-setu
   ```
2. **Say:** *"For dynamic scalability, I have configured Horizontal Pod Autoscalers (HPA) for my microservices. As you can see, the `api-gateway` and other services are actively tracking CPU and memory usage. I've set the maximum replica count to 3 so that Kubernetes can auto-scale under load."*
3. **Optional Demo (If asked):** If the examiner asks to see it scale, you can artificially spike the CPU usage and run `kubectl get hpa -n swasthya-setu -w` to watch the replicas scale up from 1 to 3!

### Step 5: Domain Specificity (Extra: 2 Marks)
**Goal:** Prove this isn't just a generic web app.

1. **Say:** *"Instead of a generic e-commerce app, this project focuses on the Healthcare domain. It includes an AI Service integrated into the microservice architecture to bring AIOps capabilities to patient and doctor portals."*

### Step 6: ELK Stack (If requested)
**Goal:** Show centralized logging.
*Note: Because Minikube is using 8GB of your RAM, you might not want ELK running in the background while compiling Java in Jenkins to avoid a crash. If the examiner asks to see ELK:*

1. **Say:** *"I have configured Logback in my Spring Boot services to stream logs directly to Logstash. To demonstrate the Kibana dashboard without starving my local laptop's memory during the Jenkins build, I can spin up the ELK stack now."*
2. **Action:** Navigate to Kibana (`http://localhost:5601`) and show the logs flowing in.

---
**Good Luck! You have an incredibly robust, professional-grade DevOps setup!**
