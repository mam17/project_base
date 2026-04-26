# ClaudeKit Setup for Android Template Project

Hướng dẫn setup ClaudeKit integration cho dự án Android.

## ✅ Đã Hoàn Thành Tích Hợp

### Thư Mục & Files Được Tạo
```
.claude/
├── CLAUDE.md                           # Instructions cho agents
├── SETUP.md                            # File này
├── rules/
│   ├── primary-workflow.md             # (từ claudekit)
│   ├── development-rules.md            # (từ claudekit)
│   ├── orchestration-protocol.md       # (từ claudekit)
│   ├── documentation-management.md     # (từ claudekit)
│   ├── team-coordination-rules.md      # (từ claudekit)
│   └── android-development-rules.md    # ⭐ NEW - Android-specific rules
├── agents/                              # (Sẵn sàng cho custom agents)
└── skills/                              # (Sẵn sàng cho custom skills)

Root Files:
├── CLAUDE.md                           # Updated với ClaudeKit integration
├── AGENTS.md                           # ⭐ NEW - Agent documentation
└── .claude/CLAUDE.md                   # Agent instructions
```

### Files Được Update
- **CLAUDE.md** - Thêm ClaudeKit Integration section & workflows
- **android-development-rules.md** - ⭐ New, tất cả quy tắc Android ở đây

## 🚀 Cách Sử Dụng ClaudeKit

### Quick Start Commands

```bash
# 1. Lập kế hoạch feature mới
/ck:plan "implement [feature_name]"

# 2. Thực thi plan (hoặc bạn implement)
/ck:cook

# 3. Kiểm thử tự động
/ck:test

# 4. Code review tự động
/ck:code-review

# 5. Cập nhật documentation
/ck:docs

# 6. Kiểm tra project status
/ck:watzup
```

## 📋 Quy Tắc BẮT BUỘC Agents Sẽ Enforce

### Trước Khi Code Bất Kỳ Điều Gì
**ĐỌC**: `./.claude/rules/android-development-rules.md`

### Base Classes - BẮT BUỘC
```kotlin
// ✅ ĐÚNG
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun initView() { }
    override fun initData() { }
}

// ❌ SAI - KHÔNG được phép
class MainActivity : AppCompatActivity() { }
```

### Layout XML - BẮT BUỘC
```xml
<!-- ✅ ĐÚNG - Có ripple effect & style -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btn_login"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Đăng Nhập"
    style="@style/Widget.MaterialComponents.Button"/>

<TextView
    android:id="@+id/tv_title"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Tiêu Đề"
    style="@style/TextStyle_Title"/>

<!-- ❌ SAI - Không có ripple, textSize hardcoded -->
<Button
    android:id="@+id/btn_login"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Đăng Nhập"
    android:textSize="16sp"/>
```

### Adapter Selection - BẮT BUỘC
Chọn adapter type phù hợp:
- `BaseAdapter<VH>` → Adapter cơ bản
- `BaseListAdapter<T>` → List đơn giản
- `BaseMultiAdapter` → Multiple item types
- `BaseMultiListAdapter` → Multi-type với list

## 📖 Documentation

### Tài Liệu Dự Án
- **CLAUDE.md** - Project setup & development guide
- **AGENTS.md** - AI agents documentation & usage
- **.claude/CLAUDE.md** - Agent instructions
- **.claude/rules/*** - Development rules & workflows

### Tài Liệu ClaudeKit
- **.claude/rules/primary-workflow.md** - Main workflow
- **.claude/rules/development-rules.md** - Dev standards
- **.claude/rules/android-development-rules.md** - Android rules
- **.claude/rules/orchestration-protocol.md** - Agent coordination

## 🔧 Workflow Example: Thêm User Profile Feature

```bash
# Step 1: Planning
claude "Tôi cần thêm User Profile screen với:
- ProfileActivity extends BaseActivity
- Layout: activity_profile.xml
- Database: UserEntity, UserDao  
- ViewModel: ProfileViewModel
- Tuân thủ ripple & style rules"

/ck:plan "implement user profile screen"
# → Planner tạo chi tiết plan (architecture, data model, UI)

# Step 2: Implementation
# Bạn hoặc Claude thực hiện:
# - Tạo ProfileActivity extends BaseActivity<ActivityProfileBinding>
# - Tạo activity_profile.xml với ripple + style
# - Tạo UserEntity, UserDao, ProfileViewModel
# - Implement initView(), initData(), initObserver()

# Step 3: Testing
/ck:test
# → Tester sinh unit tests & instrumented tests
# → Chạy tests, report coverage

# Step 4: Code Review
/ck:code-review
# → Reviewer kiểm tra:
#   ✅ ProfileActivity extends BaseActivity
#   ✅ Layout có ripple effect
#   ✅ TextViews dùng style
#   ✅ Naming conventions
#   ✅ Architecture compliance

# Step 5: Documentation
/ck:docs
# → Cập nhật README, CHANGELOG

# Done! 🎉
```

## ⚙️ Configuration

### Project Config Files
- `gradle/libs.versions.toml` - Dependencies
- `app/build.gradle.kts` - App config
- `.claude/rules/android-development-rules.md` - Android rules

### Key Settings
- **Language**: Kotlin (primary), Java (secondary)
- **Architecture**: Clean Architecture
- **DI**: Hilt
- **Database**: Room
- **UI**: ViewBinding, Material Design

## 🎯 Best Practices

### When Using Agents

1. **Be Specific in Requests**
   ```bash
   # ✅ GOOD
   /ck:plan "implement product listing with search and filter"
   
   # ❌ BAD
   /ck:plan "implement product feature"
   ```

2. **Follow Agent Recommendations**
   - Code Reviewer suggests → Fix it
   - Tester reports coverage gaps → Add tests
   - Planner identifies risks → Address them

3. **Use Agents for Quality Gates**
   - Never skip code review
   - Always run tests before commit
   - Keep documentation updated

4. **Iterate with Agents**
   ```bash
   # First iteration
   /ck:code-review  # → Issues found
   # Fix issues
   /ck:code-review  # → Clean!
   ```

## 🔍 Verify Integration

### Check These Files Exist
```bash
# Rules
ls .claude/rules/android-development-rules.md

# Documentation
ls AGENTS.md
ls CLAUDE.md
ls .claude/CLAUDE.md
ls .claude/SETUP.md

# Should see agent commands work
# Try: /ck:plan "test"
```

### Verify Agent Commands
```bash
# Should work after setup:
/ck:plan "[feature]"
/ck:cook
/ck:test
/ck:code-review
/ck:docs
/ck:watzup
```

## 📞 Support & Troubleshooting

### If Commands Don't Work
1. Check `.claude/` folder exists
2. Verify `CLAUDE.md` in root exists
3. Read `./.claude/rules/android-development-rules.md`
4. Check ClaudeKit is installed in claudekit-engineer-2.16.0/

### If Rules Not Being Enforced
1. Code Reviewer Agent needs rules files
2. Verify `./.claude/rules/android-development-rules.md` exists
3. Run `/ck:code-review` to trigger review

### If Tests Don't Generate
1. Tester Agent needs test templates
2. Check test examples exist
3. Provide detailed requirement to /ck:test

## 📚 Next Steps

1. **Read Documentation**
   - Tất cả developers đọc `./CLAUDE.md`
   - Tất cả developers đọc `./AGENTS.md`
   - Tất cả developers đọc `./.claude/rules/android-development-rules.md`

2. **Try Agent Commands**
   - Plan một feature: `/ck:plan "implement simple feature"`
   - Review code: `/ck:code-review`
   - Generate tests: `/ck:test`

3. **Integrate into Workflow**
   - Use agents cho mỗi new feature
   - Follow agent recommendations
   - Keep using agents for quality gates

4. **Customize as Needed**
   - Thêm project-specific agents nếu cần
   - Thêm custom rules nếu cần
   - Extend workflows theo team needs

---

**Integration Complete!** 🎉 ClaudeKit is now integrated with your Android template project. All agents are ready to help maintain code quality and enforce architectural standards.

**Remember**: Read `./.claude/rules/android-development-rules.md` before coding anything!
