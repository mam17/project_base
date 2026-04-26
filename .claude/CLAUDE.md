# .claude/CLAUDE.md

Hướng dẫn cho Claude Code agents khi làm việc với dự án Android này.

## Vai Trò & Trách Nhiệm

Bạn (Claude Code) có vai trò:
- Phân tích yêu cầu của user
- Ủy quyền tasks cho appropriate sub-agents
- Đảm bảo delivery có quality cao, tuân thủ architectural standards

## Workflows & Rules - BẮT BUỘC

### Đọc Trước Khi Code
1. **`./.claude/rules/android-development-rules.md`** (BẮT BUỘC)
   - Tất cả code PHẢI tuân thủ rules này
   - Base classes, Layout XML, Ripple effect, Style usage

2. **`./.claude/rules/primary-workflow.md`**
   - Primary development workflow

3. **`./.claude/rules/development-rules.md`**
   - General development standards

4. **`./.claude/rules/orchestration-protocol.md`**
   - Cách coordinate giữa agents

5. **`./CLAUDE.md` (root)**
   - Project-specific instructions

6. **`./.claude/KIT-RULES.md`** (CRITICAL)
   - ⚠️ **TUYỆT ĐỐI KHÔNG COMMIT/PUSH claudekit-engineer-2.16.0/**
   - Git rules để bảo vệ repository

### BẮT BUỘC TUÂN THỨ Android Rules
- ✅ Activity PHẢI extends `BaseActivity<VB>`
- ✅ Fragment PHẢI extends `BaseFragment`
- ✅ Adapter PHẢI extends BaseAdapter phù hợp
- ✅ Layout XML PHẢI có file riêng
- ✅ Button/ImageButton PHẢI có ripple effect
- ✅ TextView/Button/EditText PHẢI dùng style
- ✅ ViewBinding BẮT BUỘC (không findViewById)

## Key Constraints for This Android Project

### Architecture
- Clean Architecture (Data → Domain → UI)
- Hilt Dependency Injection
- Room Database
- ViewBinding
- Kotlin/Java

### Code Organization
```
app/src/main/java/com/example/myapplication/
├── base/           # Base classes (MUST extend these)
├── data/           # Data layer (Room, DAO, Entity)
├── di/             # Dependency Injection
├── domain/         # Business logic
├── ui/             # Activities, Fragments
├── utils/          # Utilities
└── views/          # Custom views
```

### Critical Files
- `./.claude/rules/android-development-rules.md` - Android-specific rules
- `./CLAUDE.md` - Project documentation
- `./app/src/main/java/com/example/myapplication/base/` - Base classes
- `./gradle/libs.versions.toml` - Dependencies

## Agents Workflow for This Project

### When Adding New Feature
1. **Planner Agent**: Create implementation plan
   - Analyze architecture
   - Plan data model (Entity/DAO)
   - Plan UI components
   - Plan UseCase logic

2. **Code Generation**: Implement following plan
   - Create Activity/Fragment extending Base
   - Create layout XML with ripple + style
   - Create Entity/DAO if needed
   - Create UseCase

3. **Tester Agent**: Generate tests
   - Unit tests for UseCase/ViewModel
   - Instrumented tests for UI

4. **Code Reviewer Agent**: Validate quality
   - Check Base class extensions
   - Verify ripple effect
   - Verify style usage
   - Check naming conventions
   - Security review

5. **Docs Manager**: Update documentation
   - Update README.md
   - Update CHANGELOG.md

## ⚠️ CRITICAL Git Rules

### TUYỆT ĐỐI KHÔNG COMMIT/PUSH KIT
- ❌ **NEVER commit**: `claudekit-engineer-2.16.0/` folder
- ✅ **.gitignore already configured** to exclude
- 📖 **Read**: `./.claude/KIT-RULES.md` for details
- ⚠️ **Before committing**: Always check `git status` - should NOT see claudekit files

### When Creating Commits
- Check staging area: `git status`
- Verify no claudekit files: `git diff --cached | grep claudekit` (should be empty)
- Only commit `.claude/rules/`, `app/`, `gradle/`, etc.

## Git Commit Convention

```bash
feat: implement user profile screen
# Activity, Fragment, layout, DAO, UseCase

fix: resolve database query crash
# Bug fix with minimal scope

refactor: improve adapter performance
# Code restructuring

test: add unit tests for UserDao
# Test additions

docs: update README with feature docs
# Documentation updates
```

**IMPORTANT**: Never commit claudekit-engineer-2.16.0/ in any of above!

## Documentation Structure

All docs should be in `./docs/` folder:
```
./docs/
├── project-overview-pdr.md    # Project overview
├── code-standards.md           # Code standards
├── codebase-summary.md         # Auto-generated summary
├── system-architecture.md      # Architecture details
└── [other docs]
```

## Important Notes

1. **Read `./.claude/rules/android-development-rules.md` FIRST** - This is critical
2. **Agents will enforce rules via code review** - No exceptions
3. **Always use Base classes** - Don't extend AppCompatActivity/Fragment directly
4. **XML Layout is mandatory** - Each screen must have layout file
5. **Ripple & Style are mandatory** - All buttons & text must follow rules

## Context for Sub-Agents

When spawning sub-agents, provide:
- Link to `./.claude/rules/android-development-rules.md`
- Link to `./CLAUDE.md` (project docs)
- Architecture context (Clean Architecture pattern)
- Specific requirements for the task

---

**Remember**: This is an Android Template Project. All code MUST follow the strict rules defined in `./.claude/rules/android-development-rules.md`. Agents are empowered to enforce these rules automatically.
