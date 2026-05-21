import urllib.request
import json

# 获取最新的运行
url = "https://api.github.com/repos/critallu/SmartNotepad/actions/runs?per_page=1"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as resp:
    runs = json.load(resp)["workflow_runs"]
    run = runs[0]
    print(f"Run ID: {run['id']}, Status: {run['status']}, Conclusion: {run['conclusion']}")
    print(f"URL: {run['html_url']}")
    print()

# 获取 jobs
url = f"https://api.github.com/repos/critallu/SmartNotepad/actions/runs/{run['id']}/jobs"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as resp:
    data = json.load(resp)
    for job in data["jobs"]:
        print(f"Job: {job['name']}")
        for step in job["steps"]:
            conclusion = step.get("conclusion", "")
            print(f"  Step: {step['name']} - {step['status']} - {conclusion}")
            if conclusion == "failure":
                print(f"    FAILED at this step!")
