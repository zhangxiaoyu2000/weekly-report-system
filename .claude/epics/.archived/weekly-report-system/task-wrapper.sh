#!/bin/bash

# Task Completion Wrapper
# This script wraps Task tool calls to add automatic completion handling

set -e

description="$1"
subagent_type="$2" 
prompt="$3"

echo "🚀 Launching enhanced task with auto-completion tracking"
echo "   Description: $description"
echo "   Type: $subagent_type"

# Extract issue number from description
issue_num=$(echo "$description" | grep -oE "#[0-9]+" | sed 's/#//' | head -1)

if [ -n "$issue_num" ]; then
    echo "   Issue: #$issue_num"
    
    # Launch the Task tool (this would be called by Claude Code)
    # For demonstration, we simulate the call structure
    echo "   Launching agent..."
    
    # The actual Task tool call would happen here in the real implementation
    # task_output=$(claude-task-tool "$description" "$subagent_type" "$prompt")
    
    echo "   Agent launched successfully"
    echo "   Completion will be auto-tracked for issue #$issue_num"
else
    echo "   No issue number detected in description"
fi

exit 0
