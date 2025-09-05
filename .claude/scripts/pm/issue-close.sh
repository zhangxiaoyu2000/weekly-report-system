#!/bin/bash

# PM System - Issue Close
# Usage: /pm:issue-close <issue-number> [epic-name]

set -e

issue_num="$1"
epic_name="$2"

echo "Closing issue..."
echo ""

if [ -z "$issue_num" ]; then
  echo "❌ Please specify an issue number"
  echo "Usage: /pm:issue-close <issue-number> [epic-name]"
  exit 1
fi

# Format issue number (pad with zeros)
# Remove any leading zeros first, then pad
issue_clean=$(echo "$issue_num" | sed 's/^0*//')
issue_padded=$(printf "%03d" "$issue_clean")

# If epic name not provided, search all epics
if [ -z "$epic_name" ]; then
  # Find the epic containing this issue
  epic_found=""
  for epic_dir in .claude/epics/*/; do
    [ -d "$epic_dir" ] || continue
    task_file="${epic_dir}${issue_padded}.md"
    if [ -f "$task_file" ]; then
      epic_found=$(basename "$epic_dir")
      break
    fi
  done
  
  if [ -z "$epic_found" ]; then
    echo "❌ Issue #$issue_num not found in any epic"
    echo ""
    echo "Available issues:"
    for epic_dir in .claude/epics/*/; do
      [ -d "$epic_dir" ] || continue
      epic_name=$(basename "$epic_dir")
      echo "  Epic: $epic_name"
      for task_file in "$epic_dir"/[0-9]*.md; do
        [ -f "$task_file" ] || continue
        task_num=$(basename "$task_file" .md)
        task_name=$(grep "^name:" "$task_file" | head -1 | sed 's/^name: *//')
        task_status=$(grep "^status:" "$task_file" | head -1 | sed 's/^status: *//')
        echo "    • #$task_num - $task_name ($task_status)"
      done
    done
    exit 1
  fi
  epic_name="$epic_found"
fi

task_file=".claude/epics/$epic_name/${issue_padded}.md"

if [ ! -f "$task_file" ]; then
  echo "❌ Issue #$issue_num not found in epic '$epic_name'"
  exit 1
fi

# Check current status
current_status=$(grep "^status:" "$task_file" | head -1 | sed 's/^status: *//')
task_name=$(grep "^name:" "$task_file" | head -1 | sed 's/^name: *//')

if [ "$current_status" = "completed" ] || [ "$current_status" = "closed" ]; then
  echo "✅ Issue #$issue_num is already closed"
  echo "   Task: $task_name"
  echo "   Status: $current_status"
  exit 0
fi

# Update status to completed
echo "📝 Updating issue status..."
if command -v sed > /dev/null 2>&1; then
  # Use sed to update status
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/^status: .*/status: completed/" "$task_file"
  else
    # Linux
    sed -i "s/^status: .*/status: completed/" "$task_file"
  fi
else
  echo "❌ sed command not available"
  exit 1
fi

# Update timestamp
current_time=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "s/^updated: .*/updated: $current_time/" "$task_file"
else
  sed -i "s/^updated: .*/updated: $current_time/" "$task_file"
fi

echo "✅ Issue #$issue_num closed successfully"
echo "   Epic: $epic_name"
echo "   Task: $task_name"
echo "   Status: open → completed"
echo ""

# Update epic progress
echo "🔄 Updating epic progress..."

# Count updated stats for this epic
epic_dir=".claude/epics/$epic_name"
total=0
completed=0

for task in "$epic_dir"/[0-9]*.md; do
  [ -f "$task" ] || continue
  ((total++))
  
  status=$(grep "^status:" "$task" | head -1 | sed 's/^status: *//')
  if [ "$status" = "completed" ] || [ "$status" = "closed" ]; then
    ((completed++))
  fi
done

# Calculate progress percentage
if [ $total -gt 0 ]; then
  progress=$((completed * 100 / total))
  echo "📊 Epic Progress: $completed/$total tasks completed ($progress%)"
  
  # Update epic.md progress field
  epic_file="$epic_dir/epic.md"
  if [ -f "$epic_file" ]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
      sed -i '' "s/^progress: .*/progress: $progress%/" "$epic_file"
    else
      sed -i "s/^progress: .*/progress: $progress%/" "$epic_file"
    fi
  fi
else
  echo "📊 No tasks found in epic"
fi

echo ""
echo "🎉 Issue closure complete!"

exit 0