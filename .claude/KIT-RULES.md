# ⚠️ CRITICAL: ClaudeKit Rules

**TUYỆT ĐỐI KHÔNG ĐƯỢC COMMIT & PUSH BỘ CLAUDEKIT**

## ❌ Quy Tắc Bắt Buộc

### KHÔNG COMMIT
```bash
❌ KHÔNG: git add claudekit-engineer-2.16.0/
❌ KHÔNG: git commit -m "update claudekit"
❌ KHÔNG: git push origin [bộ kit files]
```

### KHÔNG PUSH
```bash
❌ KHÔNG push thư mục: claudekit-engineer-2.16.0/
❌ KHÔNG push thư mục: claudekit-engineer/
❌ KHÔNG push bất kỳ files trong những thư mục trên
```

### PHẢI IGNORE
```bash
✅ .gitignore đã cấu hình:
   - claudekit-engineer-*/
   - claudekit-*/
```

## 🔍 Kiểm Tra

### Trước Khi Commit
```bash
# Kiểm tra staging area
git status

# Nếu thấy claudekit-engineer-2.16.0/, PHẢI unstage ngay
git reset HEAD claudekit-engineer-2.16.0/
git rm --cached -r claudekit-engineer-2.16.0/

# Xác nhận
git status  # Không nên thấy claudekit* files
```

### Nếu Vô Tình Thêm Vào
```bash
# 1. Unstage
git reset HEAD claudekit-engineer-2.16.0/

# 2. Xóa khỏi tracking
git rm --cached -r claudekit-engineer-2.16.0/

# 3. Thêm vào .gitignore (đã có)
# .gitignore đã cấu hình

# 4. Commit lại (không có claudekit)
git commit -m "remove claudekit from tracking"
```

## 📚 Cách Hoạt Động

### Bộ Kit Là Gì?
- **Template/Boilerplate** để setup project mới
- **Development tools** - ClaudeKit agents & workflows
- **Local config** - Cho development machine của bạn

### Tại Sao Không Commit?
1. **Lớn** - Thư mục lớn, làm repo lớn hơn
2. **Local** - Mỗi developer có bản riêng
3. **Template** - Không phải source code của app
4. **Updatable** - Có thể update separately

### Repository Chỉ Cần
- ✅ `.claude/` - Integration files (rules, CLAUDE.md)
- ✅ `app/src/main/` - Android source code
- ✅ `gradle/` - Build config
- ✅ `CLAUDE.md` - Project docs
- ✅ `AGENTS.md` - Agent docs
- ❌ `claudekit-engineer-2.16.0/` - KIT SOURCE (KHÔNG COMMIT)

## 🛑 Phòng Tránh

### Git Hooks (Optional)
Để tự động phòng tránh, bạn có thể setup pre-commit hook:

```bash
#!/bin/bash
# .git/hooks/pre-commit

if git diff --cached --name-only | grep -q "claudekit-engineer"; then
    echo "❌ ERROR: Attempting to commit claudekit files!"
    echo "ℹ️  Run: git reset HEAD claudekit-engineer-2.16.0/"
    exit 1
fi
```

### Team Guidelines
- ✅ Mỗi developer có bản claudekit-engineer riêng ở local
- ✅ Repository chỉ chứa `.claude/` (integration)
- ✅ Share qua `.claude/rules/` nếu cần update rules
- ✅ Update procedure: push rules, developers update locally

## 📋 Checklist Trước Khi Push

```bash
# ✅ Kiểm tra staging
git status

# ❌ Nên thấy: claudekit-engineer-2.16.0/
# ✅ Không nên thấy: claudekit files trong staging

# ✅ Kiểm tra diff
git diff --cached

# ❌ Không nên thấy: claudekit path changes

# ✅ Nếu tất cả ổn, commit
git commit -m "your message"

# ✅ Nếu thấy claudekit, phải fix trước
git reset HEAD claudekit-engineer-2.16.0/
```

## 🆘 Nếu Có Sự Cố

### Scenario 1: Vô Tình Thêm Vào Staging
```bash
git status
# On branch main
# Changes to be committed:
#   new file: claudekit-engineer-2.16.0/...

# Fix:
git reset HEAD claudekit-engineer-2.16.0/
git status  # Phải thấy unstaged
```

### Scenario 2: Đã Commit
```bash
# Nếu chưa push:
git reset --soft HEAD~1  # Undo commit nhưng giữ changes
git reset HEAD claudekit-engineer-2.16.0/
git commit -m "your message (without claudekit)"

# Nếu đã push:
# Contact team lead để reset remote
```

### Scenario 3: Đã Push
```bash
# Phải revert trên remote
git revert HEAD
# hoặc force reset (dangerous!)
git reset --hard HEAD~1 && git push origin main --force
```

## ⚡ TL;DR

- ⚠️ **TUYỆT ĐỐI KHÔNG COMMIT claudekit-engineer-2.16.0/**
- ✅ **.gitignore đã cấu hình** - tự động ignore
- ✅ **Repository chỉ cần .claude/ folder**
- ✅ **Kiểm tra trước commit**: `git status`
- ✅ **Nếu thêm vào**: `git reset HEAD claudekit-engineer-2.16.0/`

---

**REMEMBER**: Bộ kit là local development tool, không phải source code!
