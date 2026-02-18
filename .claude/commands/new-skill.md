---
name: new-skill
description: Create a new Claude skill with deep codebase investigation and user interview for maximum precision
argument-hint: "<skill-name or description>"
allowed-tools:
  - Read
  - Write
  - Edit
  - Bash
  - Glob
  - Grep
  - Task
  - AskUserQuestion
  - Skill
---

<objective>
Create a high-precision Claude skill by combining automated codebase investigation with structured user interviewing, then delegating to the /skill-creator for implementation.

**The goal:** Produce a skill where the trigger conditions, behavior, and implementation are so precisely defined that another Claude instance can execute it flawlessly without ambiguity.

**This command orchestrates three phases:**
1. **Investigate** — Parallel agents analyze the codebase to understand project context
2. **Interview** — Structured questioning to extract precise skill requirements from the user
3. **Create** — Hand off the complete specification to /skill-creator for implementation
</objective>

<context>
Skill request: $ARGUMENTS

**Existing skills directory:**
@.claude/skills/

**Skill creator location:**
@.claude/skills/skill-creator/SKILL.md
</context>

<process>

## Phase 0: Validate Input

1. If $ARGUMENTS is empty, ask the user: "What skill do you want to create? Describe it briefly — we'll refine it together."
2. If $ARGUMENTS is provided, acknowledge it and proceed.
3. Check `.claude/skills/` for existing skills that might overlap. If found, ask: "A skill named [X] already exists. Do you want to update it or create a new one?"

## Phase 1: Codebase Investigation (Parallel Agents)

Launch 3-4 parallel Explore agents to investigate the codebase. The agents should gather intelligence that will inform skill design. Tailor the investigation to what the user described in $ARGUMENTS.

**Agent 1 — Project Structure & Stack:**
- Identify the tech stack, frameworks, languages, build tools
- Map the project directory structure and key entry points
- Find configuration files, CI/CD setup, deployment patterns

**Agent 2 — Patterns & Conventions:**
- Identify coding conventions (naming, file organization, module patterns)
- Find testing patterns (frameworks, file locations, assertion styles)
- Discover existing automation (scripts, Makefiles, CLI tools)

**Agent 3 — Domain Context:**
- Identify the application domain (what the project does)
- Find key business logic files and data models
- Map API endpoints, services, or core abstractions

**Agent 4 — Existing Tooling & Skills (if relevant):**
- Check for existing Claude commands, skills, CLAUDE.md instructions
- Identify patterns in how the project already extends tooling
- Find any project-specific workflows or conventions documented in markdown

After all agents complete, synthesize their findings into a **Project Context Summary** — a concise internal document (do NOT write to disk) that captures:
- Tech stack and frameworks
- Key project conventions
- Domain context
- Existing automation and tooling
- Anything directly relevant to the proposed skill

Present a brief summary to the user: "Here's what I learned about your project: [2-3 bullet points]. This will help me ask better questions about the skill."

## Phase 2: Structured User Interview

Use AskUserQuestion for structured multi-select questions and free-form follow-ups. The interview has 4 rounds, each building on the previous. Do NOT ask all questions at once — pace them across rounds.

### Round 1: Trigger Precision
Goal: Define EXACTLY when this skill should activate.

Ask about:
- What exact phrases or requests should trigger this skill?
- What should NOT trigger it (negative examples)?
- Are there edge cases where it's ambiguous whether to trigger?
- Should it trigger proactively or only on explicit request?

Generate 3-4 concrete trigger examples and 2-3 anti-examples. Present them to the user for validation: "Would these trigger the skill? [examples]. Would these NOT trigger it? [anti-examples]."

### Round 2: Behavior Specification
Goal: Define EXACTLY what the skill does when triggered.

Ask about:
- What is the step-by-step workflow when the skill runs?
- What inputs does it need (files, user input, context)?
- What outputs does it produce (files, terminal output, side effects)?
- Are there decision points where behavior branches?
- What tools or external commands does it need?

Use the Project Context Summary to ask informed questions: "I see your project uses [framework/tool]. Should the skill integrate with that?"

### Round 3: Quality & Edge Cases
Goal: Define how the skill handles failures and edge cases.

Ask about:
- What happens when something goes wrong? (missing files, bad input, etc.)
- Are there prerequisites that must be met before the skill runs?
- Should the skill validate anything before proceeding?
- What does "done correctly" look like? What are the success criteria?

### Round 4: Confirmation & Refinement
Goal: Lock down the complete specification.

Present a structured summary of everything gathered:
```
SKILL SPECIFICATION
==================
Name: [skill-name]
Trigger: [when it activates]
Anti-triggers: [when it should NOT activate]
Inputs: [what it needs]
Workflow: [step by step]
Outputs: [what it produces]
Error handling: [how it handles failures]
Success criteria: [how to know it worked]
Project-specific context: [relevant codebase details]
```

Ask: "Does this capture your intent? What should I adjust?"

Iterate until the user confirms.

## Phase 3: Delegate to Skill Creator

Once the specification is confirmed:

1. Invoke the `/skill-creator` skill
2. Provide it with the complete specification, project context, and all user decisions
3. The skill-creator handles: initialization (init_skill.py), SKILL.md writing, resource creation, packaging
4. Present the final skill to the user for review

**When invoking /skill-creator, pass along:**
- The full specification from Phase 2
- Relevant project context from Phase 1 (tech stack, conventions, patterns)
- Concrete trigger examples and anti-examples
- The step-by-step workflow with decision points
- All referenced files, scripts, or assets the skill needs

## Critical Rules

- **Never skip the investigation phase.** Even for simple skills, codebase context improves precision.
- **Never skip the interview phase.** User intent cannot be assumed.
- **Always validate trigger examples.** Ambiguous triggers are the #1 cause of skill misfires.
- **Always present the specification summary** before creating anything.
- **Always use /skill-creator for implementation.** Do not write SKILL.md directly.
- **Pace questions across rounds.** Do not dump all questions in one message.
- **Use project context to ask smarter questions.** Generic questions waste the user's time.

</process>

<success_criteria>
- Codebase was investigated by parallel agents before questioning
- User was interviewed across multiple focused rounds
- Trigger conditions include both positive and negative examples
- Complete specification was presented and confirmed by user
- /skill-creator was used for actual skill implementation
- Final skill is precise enough that another Claude instance can execute it without asking clarifying questions
</success_criteria>
