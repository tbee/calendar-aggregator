if [ "$#" -ne 1 ]; then
    echo "Usage: $0 traces/<trace file>"
    exit 0
fi
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="show-trace $1"
