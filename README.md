# 📱 WeatherViewingApp - Hướng Dẫn Hoàn Chỉnh

## 🎯 Tổng Quan Dự Án

**WeatherViewingApp** là một ứng dụng Android đầy đủ tính năng, được thiết kế theo đúng chương trình học Android của bạn (Chapter 1-7).

### ✨ Tính Năng Chính

1. **Màn hình Chính (MainActivity)**
   - Hiển thị thời tiết của thành phố mặc định
   - Chuyển đổi đơn vị nhiệt độ (°C / °F)
   - Nút làm mới dữ liệu
   - Xem dữ liệu offline (cached)

2. **Màn hình Cài Đặt (SettingsActivity)**
   - Chọn đơn vị nhiệt độ (Celsius/Fahrenheit)
   - Quản lý danh sách thành phố yêu thích
   - Thêm/xóa thành phố
   - Context menu để xóa

3. **Tích Hợp API**
   - Lấy dữ liệu thời tiết thực từ OpenWeatherMap
   - Xử lý networking trong background (AsyncTask)
   - Error handling và feedback

---

## 📚 Ánh Xạ Kiến Thức Theo Chương

| Chương | Nội Dung | Áp Dụng Trong App |
|--------|----------|-------------------|
| **Ch. 1** | Android Basics | Project setup, Permissions |
| **Ch. 2** | Setup & Manifest | AndroidManifest.xml, Internet permission |
| **Ch. 3** | UI Components | ConstraintLayout, TextView, Button, ListView, RadioGroup |
| **Ch. 4** | Menu & Dialog | Options Menu, Context Menu, AlertDialog, Toast |
| **Ch. 5** | Navigation | Intent giữa MainActivity ↔ SettingsActivity |
| **Ch. 6** | Lifecycle | onCreate(), onResume(), onPause(), onDestroy() |
| **Ch. 7.a** | SharedPreferences | SettingsManager - lưu unit, default city, cache |
| **Ch. 7.c** | SQLite | DatabaseHelper - CRUD cho favorite cities |

---

## 🏗️ Kiến Trúc Dự Án

```
WeatherViewingApp/
│
├── MainActivity.java           ← Màn hình chính
├── SettingsActivity.java       ← Màn hình cài đặt
│
├── SettingsManager.java        ← Helper cho SharedPreferences
├── DatabaseHelper.java         ← Helper cho SQLite
├── WeatherApiClient.java       ← API client (HttpURLConnection + AsyncTask)
│
└── res/
    ├── layout/
    │   ├── activity_main.xml           ← UI màn hình chính
    │   ├── activity_settings.xml       ← UI màn hình cài đặt
    │   └── list_item_city.xml          ← Custom list item
    │
    ├── menu/
    │   ├── menu_main.xml               ← Options menu
    │   └── menu_context_city.xml       ← Context menu
    │
    └── values/
        ├── strings.xml         ← Text resources
        └── dimens.xml          ← Dimension resources
```

---

## 🚀 Hướng Dẫn Setup

### Bước 1: Cài Đặt Android Studio
- Tải Android Studio: https://developer.android.com/studio
- Cài đặt SDK, AVD (Android Virtual Device)

### Bước 2: Đăng Ký API Key (QUAN TRỌNG!)

**App cần API key từ OpenWeatherMap để hoạt động:**

1. Truy cập: https://openweathermap.org/api
2. Đăng ký tài khoản miễn phí
3. Tạo API key (miễn phí - 60 calls/phút)
4. Copy API key

5. **Mở file `WeatherApiClient.java` và thay thế:**
   ```java
   private static final String API_KEY = "YOUR_API_KEY_HERE"; 
   ```
   Thành:
   ```java
   private static final String API_KEY = "your_actual_api_key_here";
   ```

### Bước 3: Build & Run

1. Mở project trong Android Studio
2. Chờ Gradle sync xong
3. Chọn emulator hoặc kết nối thiết bị thật
4. Nhấn **Run** (Shift + F10)

---

## 📖 Hướng Dẫn Sử Dụng

### 1️⃣ Màn Hình Chính

**Lần đầu khởi động:**
- App sẽ hiển thị thời tiết của **Hanoi** (mặc định)
- Nếu có cache, sẽ hiển thị dữ liệu cũ trước
- Sau đó tự động tải dữ liệu mới từ API

**Làm mới dữ liệu:**
- Nhấn nút **"Làm mới"**
- Hiển thị ProgressDialog trong khi tải
- Toast thông báo khi thành công/lỗi

**Menu (3 chấm góc trên):**
- **Quản lý Thành phố** → Mở màn hình Cài đặt
- **Cài đặt** → Mở màn hình Cài đặt

### 2️⃣ Màn Hình Cài Đặt

**Chọn đơn vị nhiệt độ:**
- Chọn **Độ C** hoặc **Độ F**
- Lưu tự động vào SharedPreferences
- Khi quay lại màn hình chính, nhiệt độ sẽ được chuyển đổi

**Quản lý thành phố:**
- Nhập tên thành phố → Nhấn **Thêm**
- Dữ liệu lưu vào SQLite
- ListView hiển thị tất cả thành phố đã lưu

**Chọn thành phố mặc định:**
- Nhấn vào một thành phố trong danh sách
- Toast xác nhận
- Khi quay lại màn hình chính, app sẽ hiển thị thời tiết của thành phố này

**Xóa thành phố:**
- Nhấn **giữ** vào một thành phố
- Context menu hiện ra → Chọn **Xóa**
- AlertDialog xác nhận
- Nhấn **OK** để xóa khỏi database

---

## 🔧 Chi Tiết Kỹ Thuật

### 1. SharedPreferences (Ch. 7.a)

**File:** `SettingsManager.java`

**Lưu trữ:**
- `temperature_unit`: "C" hoặc "F"
- `default_city`: Tên thành phố mặc định
- `cached_temp`, `cached_condition`, `cached_city`: Dữ liệu cache

**Ví dụ sử dụng:**
```java
SettingsManager manager = new SettingsManager(context);
manager.setTemperatureUnit(SettingsManager.UNIT_CELSIUS);
boolean isCelsius = manager.isCelsius();
```

### 2. SQLite Database (Ch. 7.c)

**File:** `DatabaseHelper.java`

**Bảng `favorite_cities`:**
```sql
CREATE TABLE favorite_cities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    city_name TEXT NOT NULL UNIQUE
)
```

**CRUD Operations:**
```java
DatabaseHelper db = new DatabaseHelper(context);

// CREATE
boolean added = db.addCity("Paris");

// READ
List<String> cities = db.getAllCities();

// DELETE
boolean deleted = db.deleteCity("Paris");
```

### 3. Networking (HttpURLConnection + AsyncTask)

**File:** `WeatherApiClient.java`

**API Endpoint:**
```
https://api.openweathermap.org/data/2.5/weather?q={city}&appid={API_KEY}&units=metric&lang=vi
```

**Sử dụng:**
```java
WeatherApiClient client = new WeatherApiClient();
client.fetchWeather("Hanoi", new WeatherApiClient.WeatherCallback() {
    @Override
    public void onSuccess(WeatherData data) {
        // Update UI
    }
    
    @Override
    public void onError(String error) {
        // Show error
    }
});
```

### 4. Lifecycle Management (Ch. 6)

**MainActivity lifecycle:**

```java
onCreate()      → Khởi tạo views, load cache, fetch data
onResume()      → Kiểm tra settings changed, refresh nếu cần
onPause()       → Dismiss progress dialog
onDestroy()     → Clean up
```

### 5. Menu (Ch. 4)

**Options Menu:**
- Inflate từ `menu_main.xml`
- Handle trong `onOptionsItemSelected()`
- Navigate bằng Intent

**Context Menu:**
- Đăng ký: `registerForContextMenu(listView)`
- Inflate từ `menu_context_city.xml`
- Handle trong `onContextItemSelected()`

---

## 🎨 Customization

### Thêm Icon Thời Tiết Đẹp Hơn

1. Tải icon pack từ: https://openweathermap.org/weather-conditions
2. Thêm vào `res/drawable/`
3. Cập nhật method `getWeatherIcon()` trong `WeatherApiClient.java`

### Thêm Dự Báo 5 Ngày

1. Sử dụng API endpoint: `/forecast` thay vì `/weather`
2. Parse JSON array
3. Hiển thị trong ListView (đã chuẩn bị trong layout)

### Dark Mode

1. Tạo `res/values-night/colors.xml`
2. Define màu cho dark theme
3. App tự động switch theo system theme

---

## ❗ Troubleshooting

### 1. "Không thể tải dữ liệu thời tiết"

**Nguyên nhân:**
- Chưa thay API key
- Không có Internet
- API key chưa được activate (đợi vài phút sau khi đăng ký)

**Giải pháp:**
- Kiểm tra API key trong `WeatherApiClient.java`
- Bật Internet trên emulator/thiết bị
- Đợi API key được activate (~10 phút)

### 2. "NetworkOnMainThreadException"

**Nguyên nhân:**
- Chạy network call trên main thread

**Giải pháp:**
- Đã xử lý bằng AsyncTask trong code
- Nếu sửa code, đảm bảo dùng AsyncTask/Thread

### 3. ListView không hiển thị cities

**Nguyên nhân:**
- Database chưa có data

**Giải pháp:**
- Vào Settings → Thêm vài thành phố
- Kiểm tra `DatabaseHelper` hoạt động đúng

---

## 📝 Checklist Kiểm Tra

- [ ] Đã thay API key trong `WeatherApiClient.java`
- [ ] Internet permission trong AndroidManifest.xml
- [ ] SettingsActivity đã đăng ký trong Manifest
- [ ] Build thành công, không có lỗi
- [ ] Có thể xem thời tiết của thành phố
- [ ] Có thể thêm/xóa thành phố trong Settings
- [ ] Chuyển đổi C/F hoạt động
- [ ] Context menu hiển thị khi long-press
- [ ] AlertDialog xác nhận khi xóa
- [ ] Toast hiển thị feedback

---

## 🎓 Mở Rộng & Bài Tập

### Level 1 (Easy):
1. Thêm icon đẹp hơn cho thời tiết
2. Thêm màu sắc khác nhau cho temperature
3. Thêm animation khi refresh

### Level 2 (Medium):
4. Thêm dự báo 5 ngày (ListView)
5. Thêm tính năng tìm kiếm thành phố
6. Lưu lịch sử các thành phố đã xem

### Level 3 (Advanced):
7. Sử dụng RecyclerView thay ListView
8. Thêm notification cho cảnh báo thời tiết
9. Tích hợp GPS để tự động phát hiện vị trí
10. Thay AsyncTask bằng Retrofit + Coroutines

---

## 📚 Tài Liệu Tham Khảo

- **Android Developer Guide**: https://developer.android.com/guide
- **OpenWeatherMap API Docs**: https://openweathermap.org/api
- **SQLite Tutorial**: https://www.sqlitetutorial.net/
- **SharedPreferences Guide**: https://developer.android.com/training/data-storage/shared-preferences

---

## 👨‍💻 About

**Project:** WeatherViewingApp  
**Purpose:** Học Android Development (Ch. 1-7)  
**Technologies:** Java, Android SDK, SQLite, SharedPreferences, HttpURLConnection  
**API:** OpenWeatherMap  
**License:** Educational Use

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra lại API key
2. Xem lại AndroidManifest.xml (permissions)
3. Check Logcat để xem lỗi chi tiết
4. Đọc lại phần Troubleshooting ở trên

**Chúc bạn học tốt! 🚀📱**
