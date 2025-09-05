#!/bin/bash

# Agent Completion Monitor
# This script monitors Task tool completions and auto-closes issues

epic_name="$1"

if [ -z "$epic_name" ]; then
    echo "Usage: $0 <epic_name>"
    exit 1
fi

script_dir="$(dirname "$(realpath "$0")")"
pm_dir="$script_dir/../../scripts/pm"

echo "🔍 Monitoring agents for epic: $epic_name"

# Function to extract issue number from agent description
extract_issue_number() {
    local desc="$1"
    # Look for pattern like "Issue #004" or "Issue #4"
    echo "$desc" | grep -oE "#[0-9]+" | sed 's/#//' | head -1
}

# Function to check if agent completed successfully
check_agent_completion() {
    local agent_output="$1"
    local issue_num="$2"
    
    # Success indicators
    if echo "$agent_output" | grep -qi "\(✅\|completed\|finished\|success\|COMPLETE\|done\)"; then
        echo "✅ Agent completed successfully for issue #$issue_num"
        
        # Auto-close the issue
        if [ -x "$pm_dir/agent-complete.sh" ]; then
            "$pm_dir/agent-complete.sh" "$issue_num" "$epic_name" "$agent_output"
        elif [ -x "$pm_dir/issue-close.sh" ]; then
            "$pm_dir/issue-close.sh" "$issue_num" "$epic_name"
        else
            echo "⚠️  No auto-close script available, manual review needed"
        fi
        
        return 0
    elif echo "$agent_output" | grep -qi "\(❌\|failed\|error\|FAILED\|blocked\|cannot\)"; then
        echo "❌ Agent reported failure for issue #$issue_num"
        echo "   Manual review required"
        return 1
    else
        echo "⚠️  Agent completion status unclear for issue #$issue_num"
        echo "   Manual review recommended"
        return 2
    fi
}

# Main monitoring loop
echo "Starting monitoring..."
echo "Press Ctrl+C to stop monitoring"

while true; do
    # Check for new agent completions by monitoring the execution-status.md
    # In a real implementation, this would integrate with the Task tool completion callbacks
    
    # For now, this is a placeholder that would be triggered by the Task tool
    # The actual integration would happen at the Claude Code level
    
    sleep 10
done
