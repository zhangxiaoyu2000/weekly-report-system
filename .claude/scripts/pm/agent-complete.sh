#!/bin/bash

# PM System - Agent Completion Handler
# Usage: Called automatically when Task agents complete their work
# Can also be called manually: /pm:agent-complete <issue-number> [epic-name]

set -e

issue_num="$1"
epic_name="$2"
agent_output="$3"

echo "Processing agent completion..."
echo ""

if [ -z "$issue_num" ]; then
  echo "❌ Please specify an issue number"
  echo "Usage: /pm:agent-complete <issue-number> [epic-name] [agent-output]"
  exit 1
fi

# Log agent completion
echo "🤖 Agent completed work on issue #$issue_num"
if [ -n "$epic_name" ]; then
  echo "   Epic: $epic_name"
fi

# Check if agent reported success
success_indicators=("completed" "finished" "success" "✅" "COMPLETE" "done")
failure_indicators=("failed" "error" "❌" "FAILED" "blocked" "cannot")

agent_success=false
agent_failed=false

if [ -n "$agent_output" ]; then
  # Convert to lowercase for case-insensitive matching
  output_lower=$(echo "$agent_output" | tr '[:upper:]' '[:lower:]')
  
  for indicator in "${success_indicators[@]}"; do
    if echo "$output_lower" | grep -q "$indicator"; then
      agent_success=true
      break
    fi
  done
  
  for indicator in "${failure_indicators[@]}"; do
    if echo "$output_lower" | grep -q "$indicator"; then
      agent_failed=true
      break
    fi
  done
fi

# Default to success if no clear indicators (agents typically succeed)
if [ "$agent_success" = false ] && [ "$agent_failed" = false ]; then
  agent_success=true
fi

echo ""
if [ "$agent_success" = true ]; then
  echo "✅ Agent reported successful completion"
  
  # Automatically close the issue
  echo "🔄 Auto-closing issue #$issue_num..."
  
  # Call our issue-close script
  script_dir="$(dirname "$0")"
  if [ -n "$epic_name" ]; then
    "$script_dir/issue-close.sh" "$issue_num" "$epic_name"
  else
    "$script_dir/issue-close.sh" "$issue_num"
  fi
  
elif [ "$agent_failed" = true ]; then
  echo "❌ Agent reported failure or blocking issue"
  echo "   Issue #$issue_num remains open for manual review"
  
  # Log the failure for manual review
  log_file=".claude/epics/agent-failures.log"
  timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  echo "[$timestamp] Issue #$issue_num failed - Agent output: $agent_output" >> "$log_file"
  
else
  echo "⚠️  Agent completion status unclear"
  echo "   Issue #$issue_num remains open for manual review"
fi

echo ""
echo "🎯 Agent completion processing finished"

exit 0