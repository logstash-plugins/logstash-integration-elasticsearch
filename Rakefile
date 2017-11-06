task "vendor" do
  # Dummy task, to prevent Jarvis build from breaking
end

task "publish_gem" do
  exit 1 unless system("./gradlew", "clean", "publish")
end
