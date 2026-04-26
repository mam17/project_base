# Android Development Rules

Quy tắc bắt buộc cho mọi code change trong dự án Android.

## Architecture & Code Structure

### 1. Activity & Fragment
- ✅ **BẮT BUỘC**: Tất cả `Activity` phải extends `BaseActivity<VB>`
- ✅ **BẮT BUỘC**: Tất cả `Fragment` phải extends `BaseFragment` hoặc `BaseBottomFragment`
- ❌ KHÔNG được extends `AppCompatActivity` hoặc `Fragment` trực tiếp
- Implement đầy đủ lifecycle hooks: `initView()`, `initData()`, `initObserver()`

### 2. Adapter & RecyclerView
- ✅ **BẮT BUỘC**: Tất cả `Adapter` phải extends một trong các BaseAdapter:
  - `BaseAdapter<VH>` - Cho adapter cơ bản
  - `BaseListAdapter<T>` - Cho list đơn giản
  - `BaseMultiAdapter` - Cho multiple item types
  - `BaseMultiListAdapter` - Cho multi-type với list binding
- Chọn adapter type phù hợp dựa vào ngữ cảnh
- KHÔNG được extends `RecyclerView.Adapter` trực tiếp

### 3. Database (Room)
- Tạo Entity trong `data/local/entities/`
- Tạo DAO trong `data/local/dao/`
- Cập nhật `AppDatabase` để include entity mới
- Tạo Model trong `domain/layer/`
- Tạo UseCase trong `domain/usecase/`
- Tuân thủ sequence: Entity → DAO → Model → UseCase

### 4. Dependency Injection (Hilt)
- Sử dụng `@Inject` để inject dependencies
- Tất cả Activity/Fragment phải có `@AndroidEntryPoint` decorator
- Thêm provider vào `AppModule` nếu cần dependency mới

## XML Layout Rules - BẮT BUỘC

### Layout Files
- ✅ **BẮT BUỘC**: Mỗi Activity/Fragment phải có file `.xml` riêng
- Đặt tên theo quy ước:
  - Activity: `activity_[feature_name].xml`
  - Fragment: `fragment_[feature_name].xml`
  - Item: `item_[name].xml`
  - Dialog: `dialog_[name].xml`

### Ripple Effect - BẮT BUỘC
- ✅ **BẮT BUỘC**: Tất cả Button, ImageButton, clickable views phải có ripple effect
- Cách thực hiện (chọn 1):
  1. Dùng `MaterialButton` từ Material Design
  2. Thêm `android:foreground="?attr/selectableItemBackground"`
  3. Định nghĩa drawable ripple riêng
- Review: Kiểm tra visual ripple effect khi click

### Style cho Text Views - BẮT BUỘC
- ✅ **BẮT BUỘC**: Tất cả TextView, Button, EditText phải dùng `style` attribute
- ❌ KHÔNG được set trực tiếp `android:textSize`, `android:textColor`, `android:textStyle`
- Dùng styles từ `styles.xml`:
  - `TextStyle_Title` - Tiêu đề (24sp, bold)
  - `TextStyle_Body` - Nội dung chính (16sp)
  - `TextStyle_Label` - Nhãn phụ (14sp)
  - `TextStyle_Hint` - Hint text (14sp, secondary)
- Nếu cần style mới, tạo trong `styles.xml` trước

### ViewBinding - BẮT BUỘC
- ✅ **BẮT BUỘC**: Sử dụng ViewBinding, KHÔNG dùng `findViewById`
- Khai báo trong constructor của Activity/Fragment
- Sử dụng `binding.root` trong `setContentView()`

## Code Quality Rules

### Naming Conventions
- **Packages**: `com.example.myapplication.{data,domain,ui,utils}`
- **Classes**: PascalCase (ví dụ: `UserEntity`, `UserDao`, `ProfileActivity`)
- **Methods**: camelCase (ví dụ: `initView()`, `loadData()`)
- **Constants**: UPPER_SNAKE_CASE (ví dụ: `DATABASE_NAME`, `DEFAULT_TIMEOUT`)
- **Resources**: snake_case (ví dụ: `activity_profile.xml`, `color_primary`)

### File Organization
```
app/src/main/
├── java/com/example/myapplication/
│   ├── base/
│   │   ├── activity/
│   │   ├── adapter/
│   │   ├── dialog/
│   │   └── fragment/
│   ├── data/
│   │   └── local/
│   │       ├── AppDatabase.kt
│   │       ├── dao/
│   │       └── entities/
│   ├── di/
│   │   └── AppModule.kt
│   ├── domain/
│   │   ├── layer/
│   │   └── usecase/
│   ├── ui/
│   │   ├── dialog/
│   │   └── [features]/
│   ├── utils/
│   └── MyApplication.kt
└── res/
    ├── layout/
    ├── values/
    └── drawable/
```

### Code Standards
- Sử dụng Kotlin (ưu tiên hơn Java)
- Null-safety: tránh NPE bằng safe call (`?.`)
- Extension functions: tận dụng Kotlin features
- Coroutines: dùng cho async tasks thay vì RxJava (nếu có thể)
- No magic numbers: dùng constants hoặc configs

## Testing Requirements

### Unit Tests
- Tạo test cho mỗi UseCase
- Tạo test cho mỗi ViewModel
- Đặt tên: `[ClassName]Test.kt` trong `src/test/`

### Instrumented Tests
- Tạo test cho Activities/Fragments
- Dùng Espresso cho UI testing
- Đặt tên: `[ClassName]InstrumentedTest.kt` trong `src/androidTest/`

### Coverage
- Minimum 70% code coverage cho core logic
- 100% coverage cho critical features (auth, database)

## Documentation Rules

### Code Comments
- Tối thiểu: comments cho public methods
- Giải thích **WHY** chứ không phải **WHAT**
- Không duplicate với code (code đã nói lên điều gì nó làm)

### README & Docs
- Cập nhật `README.md` khi thêm feature
- Cập nhật `CLAUDE.md` nếu workflow thay đổi
- Giữ `CHANGELOG.md` up-to-date

## Git & Commit Rules

### ⚠️ TUYỆT ĐỐI KHÔNG ĐƯỢC COMMIT & PUSH KIT
- ❌ **BẮT BUỘC KHÔNG COMMIT**: Thư mục `claudekit-engineer-2.16.0/`
- ❌ **BẮT BUỘC KHÔNG PUSH**: Bộ kit này lên repository
- ✅ **PHẢI IGNORE**: `.gitignore` đã cấu hình để exclude
- ⚠️ Nếu vô tình thêm vào staging, hãy unstage ngay:
  ```bash
  git reset HEAD claudekit-engineer-2.16.0/
  git rm --cached -r claudekit-engineer-2.16.0/
  ```

**LÝ DO**: 
- Bộ kit là template/boilerplate dùng để setup project
- Nó lớn và không cần commit lên repo
- Mỗi developer có bản riêng của họ ngoài repository
- Repository chỉ cần `.claude/` (integration files) chứ không cần kit source

### Commit Messages
- Dùng conventional commits:
  - `feat: add user profile screen`
  - `fix: resolve crash on database query`
  - `refactor: improve adapter performance`
  - `test: add unit tests for UserDao`
  - `docs: update README with setup steps`
- Không sử dụng: `update`, `fix bug`, `improvements`

### Branch Strategy
- Feature: `feature/user-profile`
- Bug fix: `bugfix/login-crash`
- Hotfix: `hotfix/critical-issue`

### PR Checklist
- ✅ Tất cả Activities/Fragments extends Base classes
- ✅ Layout files đặt tên đúng
- ✅ Ripple effect trên buttons
- ✅ Style được sử dụng cho text views
- ✅ Tests được viết & pass
- ✅ Code review passed
- ✅ Documentation updated
- ✅ **KHÔNG commit/push claudekit-engineer-2.16.0/**

## Enforcement by Agents

### Code Review Agent Sẽ Kiểm Tra
- ✅ Extends từ base classes
- ✅ Ripple effect trên buttons
- ✅ Style usage cho text
- ✅ Naming conventions
- ✅ File organization

### Tester Agent Sẽ
- ✅ Sinh unit tests
- ✅ Sinh instrumented tests
- ✅ Xác nhận test pass

### Docs Manager Sẽ
- ✅ Update README.md
- ✅ Update CHANGELOG.md
- ✅ Sync documentation

---

**IMPORTANT**: Mọi code change PHẢI tuân thủ rules này. Agents sẽ enforce các rules này tự động.
