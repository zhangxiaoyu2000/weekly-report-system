#!/bin/bash

# Agent Completion Callback
# This script is called when agents complete their work

set -e

issue_num="$1"
agent_output="$2"
success="$3"

echo "🔔 Agent completion callback for issue #$issue_num"

script_dir="$(dirname "$(realpath "$0")")"
pm_dir="$script_dir/../../scripts/pm"

if [ "$success" = "true" ]; then
    echo "✅ Agent completed successfully"
    
    # Auto-close the issue
    if [ -x "$pm_dir/agent-complete.sh" ]; then
        "$pm_dir/agent-complete.sh" "$issue_num" "weekly-report-system" "$agent_output"
    else
        echo "⚠️  agent-complete.sh not found, using issue-close.sh"
        if [ -x "$pm_dir/issue-close.sh" ]; then
            "$pm_dir/issue-close.sh" "$issue_num" "weekly-report-system"
        else
            echo "❌ No completion scripts available"
        fi
    fi
else
    echo "❌ Agent reported failure or incomplete work"
    echo "   Issue #$issue_num remains open for manual review"
    
    # Log the failure
    log_file=".claude/epics/agent-failures.log"
    timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    echo "[$timestamp] Epic: weekly-report-system, Issue #$issue_num failed - $agent_output" >> "$log_file"
fi

# Update epic progress after any status change
echo "🔄 Refreshing epic progress..."
if [ -x "$pm_dir/epic-status.sh" ]; then
    "$pm_dir/epic-status.sh" "weekly-report-system"
fi

exit 0
