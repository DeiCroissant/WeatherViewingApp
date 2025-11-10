# 🔑 API Setup - QUAN TRỌNG!

## Bước 1: Đăng Ký API Key

1. Truy cập: https://openweathermap.org/api
2. Nhấn **Sign Up** (Đăng ký miễn phí)
3. Điền form đăng ký (email, password)
4. Xác nhận email
5. Đăng nhập vào tài khoản

## Bước 2: Tạo API Key

1. Vào **API keys** tab
2. Tạo key mới (hoặc dùng key mặc định)
3. Copy API key (dạng: `abc123def456ghi789...`)

⚠️ **Lưu ý:** Key mới cần ~10-15 phút để được activate!

## Bước 3: Thay API Key Trong Code

Mở file:
```
app/src/main/java/com/example/weatherviewingapp/WeatherApiClient.java
```

Tìm dòng 17:
```java
private static final String API_KEY = "YOUR_API_KEY_HERE";
```

Thay bằng:
```java
private static final String API_KEY = "abc123def456..."; // Your actual key
```

**Lưu file!**

## Bước 4: Test API

Bạn có thể test API trước trong trình duyệt:
```
https://api.openweathermap.org/data/2.5/weather?q=Hanoi&appid=YOUR_API_KEY&units=metric
```

Nếu thấy JSON response → API key đã hoạt động! ✅

## ❌ Các Lỗi Thường Gặp

**Error 401 - Unauthorized:**
- API key sai hoặc chưa được activate
- Đợi 10-15 phút rồi thử lại

**Error 429 - Too Many Requests:**
- Vượt quá 60 calls/phút (free tier)
- Đợi 1 phút rồi thử lại

**No Internet:**
- Kiểm tra kết nối mạng
- Bật Internet trên emulator

---

✅ **Sau khi setup xong, app sẽ hoạt động bình thường!**
