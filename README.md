# Weather Viewing App

## Tong quan du an

Weather Viewing App la ung dung Android xem thong tin thoi tiet cho cac thanh pho tren toan the gioi. Ung dung su dung OpenWeatherMap API de lay du lieu thoi tiet thoi gian thuc va luu tru cac thanh pho yeu thich vao SQLite database.

## Thong tin nhom phat trien

- Pham Manh Ha - Truong nhom
- Tran Minh Khoa - Thanh vien
- Tran Quang Vinh - Thanh vien

## Tinh nang chinh

### 1. Xem thong tin thoi tiet
- Hien thi nhiet do hien tai
- Hien thi trang thai thoi tiet (nang, mua, may, etc.)
- Hien thi do am, toc do gio
- Hien thi thoi gian mat troi moc va lan
- Hien thi chi so UV
- Hien thi tam nhin xa
- Du bao thoi tiet theo gio
- Du bao thoi tiet 7 ngay

### 2. Quan ly thanh pho
- Tim kiem thanh pho bang OpenWeatherMap Geocoding API
- Them thanh pho vao danh sach yeu thich
- Xoa thanh pho khoi danh sach
- Dat thanh pho mac dinh
- Them tag cho thanh pho de de quan ly
- Hien thi toa do cua thanh pho

### 3. Cai dat
- Chuyen doi don vi nhiet do (Celsius / Fahrenheit)
- Luu cau hinh nguoi dung vao SharedPreferences

### 4. Giao dien nguoi dung
- Material Design 3 voi NoActionBar theme
- Gradient background mau xanh duong tuoi sang
- Toolbar voi back navigation
- PopupMenu voi cac tuy chon menu
- Loading skeleton khi dang tai du lieu
- Error states khi khong co ket noi mang
- Swipe to refresh de cap nhat thoi tiet
- Auto-complete search cho tim kiem thanh pho

### 5. Luu tru du lieu
- SQLite database de luu danh sach thanh pho
- SharedPreferences de luu cau hinh nguoi dung
- Cache du lieu thoi tiet de xem offline

### 6. Debug
- Debug Database Activity de xem noi dung database
- Hien thi tat ca locations voi thong tin chi tiet
- Hien thi toa do, country code, default status

## Cong nghe su dung

### Android SDK
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Java 8
- Gradle 8.13

### Thu vien va Framework
- AppCompat library
- Material Design Components
- SQLite Database
- AsyncTask cho background operations
- HttpURLConnection cho API calls

### API
- OpenWeatherMap API
  - Current Weather API
  - Geocoding API
  - API Key: c087fa97752f540e360b43023b2d945a

## Cau truc du an

### Activities
- MainActivity: Man hinh chinh hien thi thoi tiet
- ManageLocationsActivity: Quan ly danh sach thanh pho
- SettingsActivity: Cai dat ung dung
- TeamActivity: Thong tin nhom phat trien
- DebugDatabaseActivity: Debug database (chi dung khi phat trien)

### Helper Classes
- DatabaseHelper: Quan ly SQLite database
- SettingsManager: Quan ly SharedPreferences
- WeatherApiClient: Xu ly API calls
- Location: Model class cho thanh pho

### Resources
- Layouts: XML layouts cho cac activities
- Drawables: Icons, backgrounds, gradients
- Menus: PopupMenu definitions
- Strings: Text resources
- Themes: Material Design 3 theme

## Database Schema

### Table: locations
- id: INTEGER PRIMARY KEY AUTOINCREMENT
- city_name: TEXT NOT NULL
- country_code: TEXT
- latitude: REAL NOT NULL
- longitude: REAL NOT NULL
- tag: TEXT
- is_default: INTEGER DEFAULT 0
- sort_order: INTEGER DEFAULT 0
- last_updated: INTEGER DEFAULT 0

### Table: favorite_cities (Legacy)
- id: INTEGER PRIMARY KEY AUTOINCREMENT
- city_name: TEXT NOT NULL UNIQUE

## SharedPreferences

### Keys
- temperature_unit: Celsius hoac Fahrenheit
- default_city: Thanh pho mac dinh
- cached_temp: Nhiet do cache
- cached_condition: Dieu kien thoi tiet cache
- cached_city: Thanh pho cache
- last_update_time: Thoi gian cap nhat cuoi

## Huong dan su dung

### 1. Xem thoi tiet
- Mo ung dung se tu dong hien thi thoi tiet cho thanh pho mac dinh (Hanoi)
- Vuot xuong de refresh du lieu thoi tiet
- Cuon xuong de xem du bao theo gio va 7 ngay

### 2. Tim kiem thanh pho
- Nhap ten thanh pho vao o tim kiem tren cung
- Chon thanh pho tu danh sach goi y
- Thoi tiet cua thanh pho se duoc hien thi

### 3. Quan ly thanh pho
- Nhan icon 3 gach (menu) o goc tren phai
- Chon "Quan ly Thanh pho"
- Nhap ten thanh pho vao o tim kiem
- Chon thanh pho tu ket qua tim kiem
- Nhan "Set Default" de dat lam thanh pho mac dinh
- Nhan "Edit Tag" de them ghi chu
- Nhan "Delete" de xoa thanh pho

### 4. Cai dat
- Nhan icon 3 gach (menu)
- Chon "Cai dat"
- Chon don vi nhiet do (Celsius hoac Fahrenheit)

### 5. Xem thong tin nhom
- Nhan icon 3 gach (menu)
- Chon "Thong tin Nhom"

### 6. Debug Database
- Nhan icon 3 gach (menu)
- Chon "Debug Database"
- Xem danh sach tat ca locations trong database

## Build va chay ung dung

### Yeu cau
- Android Studio Arctic Fox hoac moi hon
- JDK 8 hoac moi hon
- Android SDK 24 hoac moi hon

### Buoc 1: Clone project
```
git clone https://github.com/DeiCroissant/WeatherViewingApp.git
```

### Buoc 2: Mo trong Android Studio
- Mo Android Studio
- Chon "Open an existing project"
- Chon thu muc WeatherViewingApp

### Buoc 3: Sync Gradle
- Android Studio se tu dong sync Gradle
- Neu khong, chon File > Sync Project with Gradle Files

### Buoc 4: Chay ung dung
- Ket noi thiet bi Android hoac mo emulator
- Nhan Run (Shift+F10)

### Buoc 5: Build APK
- Chon Build > Build Bundle(s) / APK(s) > Build APK(s)
- APK se duoc tao trong thu muc app/build/outputs/apk/debug/

## Cau hinh API Key

Neu muon su dung API key cua ban:
1. Dang ky tai https://openweathermap.org/api
2. Lay API key
3. Mo WeatherApiClient.java
4. Thay doi gia tri API_KEY

## Luu y quan trong

### Su dung mang
- Ung dung can ket noi internet de lay du lieu thoi tiet
- Du lieu se duoc cache de xem offline

### Quyen truy cap
- INTERNET: De goi API
- ACCESS_NETWORK_STATE: De kiem tra ket noi mang

### Database version
- Database version hien tai: 2
- Khi nang cap database, tang DATABASE_VERSION trong DatabaseHelper.java

## Cac van de da duoc fix

### 1. Default city khong cap nhat
- Van de: Khi chon thanh pho lam mac dinh, MainActivity khong cap nhat
- Nguyen nhan: fetchWeatherData() reset currentCity ve "Hanoi"
- Giai phap: Xoa dong reset, su dung currentCity tu SettingsManager

### 2. City management trong Settings
- Van de: City management o ca Settings va ManageLocations
- Giai phap: Xoa city management khoi Settings, chi giu trong ManageLocations

### 3. Debug Database hien thi sai
- Van de: DebugDatabaseActivity doc tu bang favorite_cities (cu)
- Giai phap: Cap nhat de doc tu bang locations (moi)

### 4. API calls that bai
- Van de: Su dung http thay vi https, API key sai
- Giai phap: Cap nhat thanh https, dung API key dung

### 5. PopupMenu khong hien thi icons
- Van de: Android mac dinh khong hien thi icons trong PopupMenu
- Giai phap: Su dung Java reflection de force show icons

## Ke hoach tuong lai

### Tinh nang se them
- Notification cho canh bao thoi tiet
- Widget tren home screen
- Dark mode
- Nhieu ngon ngu (Tieng Viet, Tieng Anh)
- Chart hien thi bieu do nhiet do
- Chia se thong tin thoi tiet len social media

### Cai tien UI/UX
- Animation khi chuyen doi giua cac man hinh
- Loading states tot hon
- Error handling tot hon
- Accessibility improvements

### Toi uu hoa
- Giam thoi gian load du lieu
- Cache thong minh hon
- Nen hinh anh va resource
- Giam kich thuoc APK

## Lien he

Neu co bat ky thac mac nao, vui long lien he:
- GitHub: https://github.com/DeiCroissant/WeatherViewingApp
- Email: support@weatherapp.com

## Giay phep

MIT License

Copyright (c) 2025 Weather Viewing App Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Changelog

### Version 1.0.0 (2025-11-13)
- Release ban dau
- Tinh nang xem thoi tiet co ban
- Quan ly thanh pho
- Cai dat don vi nhiet do
- Gradient background xanh duong
- PopupMenu voi icons
- Debug database

### Version 0.9.0 (Development)
- Them TeamActivity
- Fix default city selection
- Xoa city management khoi Settings
- Fix DebugDatabaseActivity
- Cap nhat API endpoint
- Them logging cho debug

## Credits

- OpenWeatherMap API cho du lieu thoi tiet
- Material Design Icons
- Android Developer Documentation
- Stack Overflow community
