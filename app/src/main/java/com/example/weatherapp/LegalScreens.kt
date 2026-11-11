package com.example.weatherapp

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlin.random.Random

fun isLegalAccepted(context: Context): Boolean {
    return context.getSharedPreferences("app_translator_cache", Context.MODE_PRIVATE)
        .getBoolean("is_legal_accepted", false)
}

fun setLegalAccepted(context: Context, accepted: Boolean) {
    context.getSharedPreferences("app_translator_cache", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("is_legal_accepted", accepted)
        .apply()
}

@Composable
fun getTranslatedText(key: String): String {
    val lang = AppTranslator.currentLanguage

    val texts = mapOf(
        "policy_title" to mapOf(
            "ru" to "Политика конфиденциальности",
            "kk" to "Құпиялылық саясаты",
            "en" to "Privacy Policy",
            "pt" to "Política de Privacidade",
            "de" to "Datenschutzrichtlinie",
            "fr" to "Politique de confidentialité",
            "it" to "Informativa sulla privacy",
            "uz" to "Maxfiylik siyosati",
            "tr" to "Gizlilik Politikası",
            "zh-CN" to "隐私政策",
            "fr-CA" to "Politique de confidentialité",
            "es" to "Política de privacidad",
            "ka" to "კონფიდენციალურობის პოლიტიკა",
            "vi" to "Chính sách bảo mật"
        ),
        "policy_text" to mapOf(
            "ru" to "Пункт 1. Общие положения и безопасность данных при использовании WeatherApp.\n\nПункт 2. Геолокация (GPS): координаты (ACCESS_FINE_LOCATION и ACCESS_COARSE_LOCATION) запрашиваются только по инициативе пользователя для получения прогноза погоды в текущей точке и не передаются рекламным или сторонним аналитическим сетям.\n\nПункт 3. Доступ к галерее и памяти: выбранное фото профиля копируется исключительно во внутреннюю изолированную память приложения на устройстве (filesDir) и никогда не выгружается на внешние серверы.\n\nПункт 4. Локальное хранение и сброс данных: список городов, выбранный язык, тема оформления и данные профиля хранятся локально на устройстве (Room Database, DataStore и SharedPreferences). Удаление данных приложения или удаление приложения полностью стирает эти данные.",
            "kk" to "1-тармақ. WeatherApp қолданбасын пайдалану кезіндегі жалпы ережелер және деректер қауіпсіздігі.\n\n2-тармақ. Геолокация (GPS): координаттар (ACCESS_FINE_LOCATION және ACCESS_COARSE_LOCATION) ағымдағы нүктеде ауа райы болжамын алу үшін тек пайдаланушының бастамасы бойынша сұралады және жарнамалық немесе үшінші тарап аналитикалық желілеріне берілмейді.\n\n3-тармақ. Галереяға және жадқа қол жеткізу: таңдалған профиль фотосуреті тек құрылғыдағы қолданбаның ішкі оқшауланған жадына (filesDir) көшіріледі және ешқашан сыртқы серверлерге жүктелмейді.\n\n4-тармақ. Деректерді жергілікті сақтау және қалпына келтіру: қалалар тізімі, таңдалған тіл, тақырып және профиль деректері құрылғыда жергілікті түрде сақталады (Room Database, DataStore және SharedPreferences). Қолданба деректерін немесе қолданбаны толығымен жою бұл деректерді өшіреді.",
            "en" to "Point 1. General provisions and data safety when using WeatherApp.\n\nPoint 2. Geolocation (GPS): coordinates (ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION) are requested only on the user's initiative to get the weather forecast at the current location and are not transferred to advertising or third-party analytical networks.\n\nPoint 3. Access to gallery and storage: the selected profile photo is copied exclusively to the app's internal isolated storage on the device (filesDir) and is never uploaded to external servers.\n\nPoint 4. Local storage and data reset: the list of cities, selected language, theme, and profile data are stored locally on the device (Room Database, DataStore, and SharedPreferences). Clearing app data or uninstalling the app completely erases this data.",
            "pt" to "Ponto 1. Disposições gerais e segurança de dados ao usar o WeatherApp.\n\nPonto 2. Geolocalização (GPS): coordenadas (ACCESS_FINE_LOCATION e ACCESS_COARSE_LOCATION) são solicitadas apenas por iniciativa do usuário para obter a previsão do tempo no local atual e não são transferidas para redes publicitárias ou analíticas de terceiros.\n\nPonto 3. Acesso à galeria e armazenamento: a foto de perfil selecionada é copiada exclusivamente para o armazenamento isolado interno do aplicativo no dispositivo (filesDir) e nunca é enviada para servidores externos.\n\nPonto 4. Armazenamento local e redefinição de dados: a lista de cidades, idioma selecionado, tema e dados do perfil são armazenados localmente no dispositivo (Room Database, DataStore e SharedPreferences). Limpar os dados do aplicativo ou desinstalar o aplicativo apaga completamente esses dados.",
            "de" to "Punkt 1. Allgemeine Bestimmungen und Datensicherheit bei der Nutzung der WeatherApp.\n\nPunkt 2. Geolokalisierung (GPS): Koordinaten (ACCESS_FINE_LOCATION und ACCESS_COARSE_LOCATION) werden nur auf Initiative des Nutzers abgefragt, um die Wettervorhersage für den aktuellen Standort zu erhalten, und werden nicht an Werbe- oder Analyse-Netzwerke Dritter weitergegeben.\n\nPunkt 3. Zugriff auf Galerie und Speicher: Das ausgewählte Profilfoto wird ausschließlich in den internen, isolierten Speicher der App auf dem Gerät (filesDir) kopiert und niemals auf externe Server hochgeladen.\n\nPunkt 4. Lokale Speicherung und Datenlöschung: Die Liste der Städte, die ausgewählte Sprache, das Design und die Profildaten werden lokal auf dem Gerät gespeichert (Room Database, DataStore und SharedPreferences). Das Löschen der App-Daten oder die Deinstallation der App löscht diese Daten vollständig.",
            "fr" to "Point 1. Dispositions générales et sécurité des données lors de l'utilisation de WeatherApp.\n\nPoint 2. Géolocalisation (GPS) : les coordonnées (ACCESS_FINE_LOCATION et ACCESS_COARSE_LOCATION) sont demandées uniquement à l'initiative de l'utilisateur pour obtenir les prévisions météo à l'emplacement actuel et ne sont pas transférées à des réseaux publicitaires ou d'analyse tiers.\n\nPoint 3. Accès à la galerie et au stockage : la photo de profil sélectionnée est copiée exclusivement dans le stockage interne isolé de l'application sur l'appareil (filesDir) et n'est jamais téléchargée sur des serveurs externes.\n\nPoint 4. Stockage local et réinitialisation des données : la liste des villes, la langue sélectionnée, le thème et les données de profil sont stockés localement sur l'appareil (Room Database, DataStore et SharedPreferences). L'effacement des données de l'application ou sa désinstallation efface complètement ces données.",
            "it" to "Punto 1. Disposizioni generali e sicurezza dei dati durante l'utilizzo di WeatherApp.\n\nPunto 2. Geolocalizzazione (GPS): le coordinate (ACCESS_FINE_LOCATION e ACCESS_COARSE_LOCATION) sono richieste solo su iniziativa dell'utente per ottenere le previsioni del tempo nella posizione attuale e non vengono trasferite a reti pubblicitarie o analitiche di terze parti.\n\nPunto 3. Accesso alla galleria e alla memoria: la foto del profilo selezionata viene copiata esclusivamente nella memoria interna isolata dell'app sul dispositivo (filesDir) e non viene mai caricata su server esterni.\n\nPunto 4. Archiviazione locale e ripristino dei dati: l'elenco delle città, la lingua selezionata, il tema e i dati del profilo sono memorizzati localmente sul dispositivo (Room Database, DataStore e SharedPreferences). La cancellazione dei dati dell'app o la disinstallazione dell'app cancella completamente questi dati.",
            "uz" to "1-band. WeatherApp-dan foydalanishda umumiy qoidalar va ma'lumotlar xavfsizligi.\n\n2-band. Geolokatsiya (GPS): koordinatalar (ACCESS_FINE_LOCATION va ACCESS_COARSE_LOCATION) faqat foydalanuvchining tashabbusi bilan joriy nuqtada ob-havo ma'lumotlarini olish uchun so'raladi va reklama yoki uchinchi tomon analitik tarmoqlariga o'tkazilmaydi.\n\n3-band. Galereya va xotiraga kirish: tanlangan profil surati faqat qurilmadagi ilovaning ichki ajratilgan xotirasiga (filesDir) nusxalanadi va hech qachon tashqi serverlarga yuklanmaydi.\n\n4-band. Ma'lumotlarni mahalliy saqlash va tiklash: shaharlar ro'yxati, tanlangan til, mavzu va profil ma'lumotlari qurilmada mahalliy tarzda saqlanadi (Room Database, DataStore va SharedPreferences). Ilova ma'lumotlarini tozalash yoki ilovani o'chirish ushbu ma'lumotlarni butunlay o'chirib tashlaydi.",
            "tr" to "Madde 1. WeatherApp kullanımında genel hükümler ve veri güvenliği.\n\nMadde 2. Geolokasyon (GPS): koordinatlar (ACCESS_FINE_LOCATION ve ACCESS_COARSE_LOCATION) yalnızca kullanıcının girişimiyle mevcut konumdaki hava durumu tahminini almak için talep edilir ve reklam veya üçüncü taraf analitik ağlarına aktarılmaz.\n\nMadde 3. Galeriye ve depolama alanına erişim: seçilen profil fotoğrafı yalnızca cihazdaki uygulamanın dahili yalıtılmış depolama alanına (filesDir) kopyalanır ve hiçbir zaman harici sunuculara yüklenmez.\n\nMadde 4. Yerel depolama ve veri sıfırlama: şehirlerin listesi, seçilen dil, tema ve profil verileri cihazda yerel olarak depolanır (Room Database, DataStore ve SharedPreferences). Uygulama verilerini temizlemek veya uygulamayı kaldırmak bu verileri tamamen siler.",
            "zh-CN" to "第一条。使用 WeatherApp 时的总则和数据安全。\n\n第二条。地理定位 (GPS)：仅在用户主动要求获取当前位置的天气预报时才请求坐标（ACCESS_FINE_LOCATION 和 ACCESS_COARSE_LOCATION），并且不会转移到广告或第三方分析网络。\n\n第三条。访问图库和存储空间：选定的个人资料照片仅复制到设备上应用程序的内部隔离存储空间 (filesDir)，绝不会上传到外部服务器。\n\n第四条。本地存储和数据重置：城市列表、所选语言、主题和个人资料数据存储在本地设备上（Room Database、DataStore 和 SharedPreferences）。清除应用程序数据或完全卸载应用程序会清除这些数据。",
            "fr-CA" to "Point 1. Dispositions générales et sécurité des données lors de l'utilisation de WeatherApp.\n\nPoint 2. Géolocalisation (GPS) : les coordonnées (ACCESS_FINE_LOCATION et ACCESS_COARSE_LOCATION) sont demandées uniquement à l'initiative de l'utilisateur pour obtenir les prévisions météo à l'emplacement actuel et ne sont pas transférées à des réseaux publicitaires ou d'analyse tiers.\n\nPoint 3. Accès à la galerie et au stockage : la photo de profil sélectionnée est copiée exclusivement dans le stockage interne isolé de l'application sur l'appareil (filesDir) et n'est jamais téléchargée sur des serveurs externes.\n\nPoint 4. Stockage local et réinitialisation des données : la liste des villes, la langue sélectionnée, le thème et les données de profil sont stockés localement sur l'appareil (Room Database, DataStore et SharedPreferences). L'effacement des données de l'application ou sa désinstallation efface complètement ces données.",
            "es" to "Punto 1. Disposiciones generales y seguridad de datos al usar WeatherApp.\n\nPunto 2. Geolocalización (GPS): las coordenadas (ACCESS_FINE_LOCATION y ACCESS_COARSE_LOCATION) se solicitan únicamente por iniciativa del usuario para obtener el pronóstico del tiempo en la ubicación actual y no se transfieren a redes publicitarias o analíticas de terceros.\n\nPunto 3. Acceso a la galería y al almacenamiento: la foto de perfil seleccionada se copia exclusivamente en el almacenamiento aislado interno de la aplicación en el dispositivo (filesDir) y nunca se carga en servidores externos.\n\nPunto 4. Almacenamiento local y restablecimiento de datos: la lista de ciudades, el idioma seleccionado, el tema y los datos del perfil se almacenan localmente en el dispositivo (Room Database, DataStore y SharedPreferences). Borrar los datos de la aplicación o desinstalar la aplicación borra por completo estos datos.",
            "ka" to "პუნქტი 1. ზოგადი დებულებები და მონაცემთა უსაფრთხოება WeatherApp-ის გამოყენებისას.\n\nპუნქტი 2. გეოლოკაცია (GPS): კოორდინატები (ACCESS_FINE_LOCATION და ACCESS_COARSE_LOCATION) მოითხოვება მხოლოდ მომხმარებლის ინიციატივით, რათა მიიღოს ამინდის პროგნოზი მიმდინარე ლოკაციაზე და არ გადაეცემა სარეკლამო ან მესამე მხარის ანალიტიკურ ქსელებს.\n\nპუნქტი 3. გალერეაზე და მეხსიერებაზე წვდომა: შერჩეული პროფილის ფოტო კოპირდება ექსკლუზიურად აპლიკაციის შიდა იზოლირებულ მეხსიერებაში მოწყობილობაზე (filesDir) და არასოდეს იტვირთება გარე სერვერებზე.\n\nპუნქტი 4. ლოკალური შენახვა და მონაცემების გადატვირთვა: ქალაქების სია, არჩეული ენა, თემა და პროფილის მონაცემები ინახება ლოკალურად მოწყობილობაზე (Room Database, DataStore და SharedPreferences). აპლიკაციის მონაცემების გასუფთავება ან აპლიკაციის დეინსტალაცია მთლიანად შლის ამ მონაცემებს.",
            "vi" to "Điểm 1. Quy định chung và an toàn dữ liệu khi sử dụng WeatherApp.\n\nĐiểm 2. Vị trí địa lý (GPS): tọa độ (ACCESS_FINE_LOCATION và ACCESS_COARSE_LOCATION) chỉ được yêu cầu theo chủ ý của người dùng để lấy dự báo thời tiết tại vị trí hiện tại và không được chuyển cho quảng cáo hoặc mạng phân tích của bên thứ ba.\n\nĐiểm 3. Truy cập vào thư viện và bộ nhớ: ảnh hồ sơ đã chọn được sao chép độc quyền vào bộ nhớ cách ly bên trong của ứng dụng trên thiết bị (filesDir) và không bao giờ được tải lên máy chủ bên ngoài.\n\nĐiểm 4. Lưu trữ cục bộ và đặt lại dữ liệu: danh sách các thành phố, ngôn ngữ đã chọn, chủ đề và dữ liệu hồ sơ được lưu trữ cục bộ trên thiết bị (Room Database, DataStore và SharedPreferences). Xóa dữ liệu ứng dụng hoặc gỡ cài đặt ứng dụng sẽ xóa hoàn toàn dữ liệu này."
        ),
        "policy_checkbox" to mapOf(
            "ru" to "Я прочитал(а) и согласен(на) с Политикой конфиденциальности",
            "kk" to "Мен Құпиялылық саясатын оқыдым және келісемін",
            "en" to "I have read and agree to the Privacy Policy",
            "pt" to "Li e concordo com a Política de Privacidade",
            "de" to "Ich habe die Datenschutzrichtlinie gelesen und stimme ihr zu",
            "fr" to "J'ai lu et j'accepte la politique de confidentialité",
            "it" to "Ho letto e accetto l'Informativa sulla privacy",
            "uz" to "Men Maxfiylik siyosatini o'qidim va roziman",
            "tr" to "Gizlilik Politikasını okudum ve kabul ediyorum",
            "zh-CN" to "我已阅读并同意隐私政策",
            "fr-CA" to "J'ai lu et j'accepte la politique de confidentialité",
            "es" to "He leído y acepto la Política de privacidad",
            "ka" to "წავიკითხე და ვეთანხმები კონფიდენციალურობის პოლიტიკას",
            "vi" to "Tôi đã đọc và đồng ý với Chính sách bảo mật"
        ),
        "btn_next" to mapOf(
            "ru" to "Далее",
            "kk" to "Келесі",
            "en" to "Next",
            "pt" to "Próximo",
            "de" to "Weiter",
            "fr" to "Suivant",
            "it" to "Avanti",
            "uz" to "Keyingisi",
            "tr" to "İleri",
            "zh-CN" to "下一步",
            "fr-CA" to "Suivant",
            "es" to "Siguiente",
            "ka" to "შემდეგი",
            "vi" to "Tiếp theo"
        ),
        "btn_disagree" to mapOf(
            "ru" to "Не согласен (Выйти)",
            "kk" to "Келіспеймін (Шығу)",
            "en" to "Disagree (Exit)",
            "pt" to "Discordo (Sair)",
            "de" to "Nicht einverstanden (Beenden)",
            "fr" to "Pas d'accord (Quitter)",
            "it" to "Non accetto (Esci)",
            "uz" to "Rozi emasman (Chiqish)",
            "tr" to "Kabul Etmiyorum (Çıkış)",
            "zh-CN" to "不同意（退出）",
            "fr-CA" to "Pas d'accord (Quitter)",
            "es" to "No estoy de acuerdo (Salir)",
            "ka" to "არ ვეთანხმები (გასვლა)",
            "vi" to "Không đồng ý (Thoát)"
        ),
        "terms_title" to mapOf(
            "ru" to "Пользовательское соглашение",
            "kk" to "Пайдаланушы келісімі",
            "en" to "Terms of Use",
            "pt" to "Termos de Uso",
            "de" to "Nutzungsbedingungen",
            "fr" to "Conditions d'utilisation",
            "it" to "Termini di utilizzo",
            "uz" to "Foydalanish shartlari",
            "tr" to "Kullanım Koşulları",
            "zh-CN" to "使用条款",
            "fr-CA" to "Conditions d'utilisation",
            "es" to "Términos de uso",
            "ka" to "Მოხმარების პირობები",
            "vi" to "Điều khoản sử dụng"
        ),
        "terms_text" to mapOf(
            "ru" to "Пункт 1. Принятие условий: использование приложения возможно только при полном согласии с настоящим Соглашением.\n\nПункт 2. Источник метеоданных и лицензия Open-Meteo: все метеорологические данные предоставляются сторонним сервисом Open-Meteo API (https://open-meteo.com/) по лицензии CC BY 4.0. Разработчик приложения не является владельцем метеостанций и не гарантирует бесперебойную работу стороннего API.\n\nПункт 3. Таймер задержки обновления (2 часа): пользователь уведомлён и согласен с тем, что в приложении встроен программный таймер кэширования погоды на 2 (два) часа. Погодные данные обновляются не чаще одного раза в 2 часа для оптимизации сетевой нагрузки, поэтому отображаемая температура и погодные коды могут иметь задержку и отличаться от фактических погодных условий в реальном времени.\n\nПункт 4. Использование на свой страх и риск (Отказ от ответственности / «AS IS»): приложение, прогнозы погоды и автоматические советы по выбору одежды предоставляются по принципу «как есть» исключительно в ознакомительных целях. Пользователь использует приложение полностью на свой страх и риск. Разработчик не несёт никакой юридической, материальной или иной ответственности за любые прямые или косвенные убытки, дискомфорт, вред здоровью или имуществу, вызванные несовпадением прогноза с реальной погодой или двухчасовой задержкой обновления данных.",
            "kk" to "1-тармақ. Шарттарды қабылдау: қолданбаны пайдалану осы Келісіммен толық келіскен жағдайда ғана мүмкін болады.\n\n2-тармақ. Ауа райы деректерінің көзі және Open-Meteo лицензиясы: барлық метеорологиялық деректерді CC BY 4.0 лицензиясы бойынша Open-Meteo API (https://open-meteo.com/) үшінші тарап қызметі ұсынады. Қолданбаны әзірлеуші метеостанциялардың иесі емес және үшінші тарап API үздіксіз жұмыс істеуіне кепілдік бермейді.\n\n3-тармақ. Жаңартуды кідірту таймері (2 сағат): пайдаланушы қолданбада 2 (екі) сағатқа арналған ауа райын кэштеу бағдарламалық таймері орнатылғаны туралы хабарланған және келіседі. Ауа райы деректері желі жүктемесін оңтайландыру үшін әр 2 сағат сайын бір реттен жиі жаңартылмайды, сондықтан көрсетілген температура мен ауа райы кодтары кешіктірілуі және нақты уақыттағы нақты ауа райы жағдайынан өзгеше болуы мүмкін.\n\n4-тармақ. Өз тәуекеліңізбен пайдалану (Жауапкершіліктен бас тарту / «AS IS»): қолданба, ауа райы болжамдары және киім таңдау бойынша автоматты кеңестер тек ақпараттық мақсаттар үшін «қалай болса, солай» негізінде беріледі. Пайдаланушы қолданбаны толығымен өз тәуекелімен пайдаланады. Әзірлеуші болжамның нақты ауа райымен сәйкес келмеуі немесе деректерді жаңартудың екі сағаттық кешігуінен туындаған кез келген тікелей немесе жанама шығындар, қолайсыздықтар, денсаулыққа немесе мүлікке зиян үшін ешқандай заңды, материалдық немесе басқа жауапкершілік көтермейді.",
            "en" to "Point 1. Acceptance of terms: the use of the app is possible only upon full agreement with this Agreement.\n\nPoint 2. Source of weather data and Open-Meteo license: all meteorological data is provided by a third-party service Open-Meteo API (https://open-meteo.com/) under the CC BY 4.0 license. The app developer is not the owner of weather stations and does not guarantee the uninterrupted operation of the third-party API.\n\nPoint 3. Update delay timer (2 hours): the user is notified and agrees that the app has a built-in software timer for caching weather for 2 (two) hours. Weather data is updated no more than once every 2 hours to optimize network load, so the displayed temperature and weather codes may have a delay and differ from actual weather conditions in real-time.\n\nPoint 4. Use at your own risk (Disclaimer / \"AS IS\"): the app, weather forecasts, and automatic clothing tips are provided on an \"as is\" basis strictly for informational purposes. The user uses the app entirely at their own risk. The developer bears no legal, financial, or other liability for any direct or indirect losses, discomfort, harm to health or property caused by a mismatch between the forecast and actual weather or a two-hour delay in updating data.",
            "pt" to "Ponto 1. Aceitação dos termos: o uso do aplicativo só é possível mediante total concordância com este Contrato.\n\nPonto 2. Fonte de dados meteorológicos e licença Open-Meteo: todos os dados meteorológicos são fornecidos por um serviço terceirizado Open-Meteo API (https://open-meteo.com/) sob a licença CC BY 4.0. O desenvolvedor do aplicativo não é o proprietário das estações meteorológicas e não garante o funcionamento ininterrupto da API de terceiros.\n\nPonto 3. Temporizador de atraso de atualização (2 horas): o usuário é notificado e concorda que o aplicativo possui um temporizador de software integrado para armazenar em cache o clima por 2 (duas) horas. Os dados meteorológicos são atualizados no máximo uma vez a cada 2 horas para otimizar a carga da rede, portanto, a temperatura e os códigos meteorológicos exibidos podem ter um atraso e diferir das condições climáticas reais em tempo real.\n\nPonto 4. Use por sua conta e risco (Isenção de responsabilidade / \"COMO ESTÁ\"): o aplicativo, previsões meteorológicas e dicas automáticas de roupas são fornecidos \"como estão\" estritamente para fins informativos. O usuário usa o aplicativo inteiramente por sua própria conta e risco. O desenvolvedor não assume nenhuma responsabilidade legal, financeira ou outra por quaisquer perdas diretas ou indiretas, desconforto, danos à saúde ou à propriedade causados por uma incompatibilidade entre a previsão e o clima real ou um atraso de duas horas na atualização dos dados.",
            "de" to "Punkt 1. Annahme der Bedingungen: Die Nutzung der App ist nur bei vollständiger Zustimmung zu dieser Vereinbarung möglich.\n\nPunkt 2. Quelle der Wetterdaten und Open-Meteo-Lizenz: Alle meteorologischen Daten werden von einem Drittanbieter-Dienst Open-Meteo API (https://open-meteo.com/) unter der CC BY 4.0-Lizenz bereitgestellt. Der App-Entwickler ist nicht der Eigentümer von Wetterstationen und garantiert nicht den ununterbrochenen Betrieb der Drittanbieter-API.\n\nPunkt 3. Verzögerungs-Timer für Updates (2 Stunden): Der Benutzer wird darüber informiert und stimmt zu, dass in die App ein Software-Timer zum Zwischenspeichern des Wetters für 2 (zwei) Stunden integriert ist. Wetterdaten werden zur Optimierung der Netzwerkauslastung nicht öfter als alle 2 Stunden aktualisiert, sodass die angezeigte Temperatur und die Wettercodes verzögert sein und von den tatsächlichen Wetterbedingungen in Echtzeit abweichen können.\n\nPunkt 4. Nutzung auf eigene Gefahr (Haftungsausschluss / \"AS IS\"): Die App, Wettervorhersagen und automatische Bekleidungstipps werden \"wie besehen\" und ausschließlich zu Informationszwecken zur Verfügung gestellt. Der Benutzer nutzt die App auf eigene Gefahr. Der Entwickler übernimmt keine rechtliche, finanzielle oder sonstige Haftung für direkte oder indirekte Verluste, Unbehagen, Gesundheits- oder Sachschäden, die durch eine Diskrepanz zwischen der Vorhersage und dem tatsächlichen Wetter oder eine zweistündige Verzögerung bei der Aktualisierung der Daten entstehen.",
            "fr" to "Point 1. Acceptation des conditions : l'utilisation de l'application n'est possible qu'avec le plein accord à cet Accord.\n\nPoint 2. Source des données météorologiques et licence Open-Meteo : toutes les données météorologiques sont fournies par un service tiers Open-Meteo API (https://open-meteo.com/) sous la licence CC BY 4.0. Le développeur de l'application n'est pas le propriétaire des stations météorologiques et ne garantit pas le fonctionnement ininterrompu de l'API tierce.\n\nPoint 3. Minuterie de délai de mise à jour (2 heures) : l'utilisateur est informé et accepte que l'application intègre une minuterie logicielle de mise en cache de la météo pour 2 (deux) heures. Les données météorologiques ne sont pas mises à jour plus d'une fois toutes les 2 heures pour optimiser la charge du réseau, ainsi la température et les codes météorologiques affichés peuvent avoir un délai et différer des conditions météorologiques réelles en temps réel.\n\nPoint 4. Utilisation à vos propres risques (Clause de non-responsabilité / \"EN L'ÉTAT\") : l'application, les prévisions météorologiques et les conseils vestimentaires automatiques sont fournis \"en l'état\" strictement à des fins d'information. L'utilisateur utilise l'application entièrement à ses propres risques. Le développeur n'assume aucune responsabilité légale, financière ou autre pour les pertes directes ou indirectes, l'inconfort, les dommages à la santé ou aux biens causés par une inadéquation entre la prévision et la météo réelle ou un délai de deux heures dans la mise à jour des données.",
            "it" to "Punto 1. Accettazione dei termini: l'uso dell'app è possibile solo previa completa accettazione del presente Accordo.\n\nPunto 2. Fonte dei dati meteorologici e licenza Open-Meteo: tutti i dati meteorologici sono forniti da un servizio di terze parti Open-Meteo API (https://open-meteo.com/) sotto la licenza CC BY 4.0. Lo sviluppatore dell'app non è il proprietario delle stazioni meteorologiche e non garantisce il funzionamento ininterrotto dell'API di terze parti.\n\nPunto 3. Timer di ritardo dell'aggiornamento (2 ore): l'utente viene informato e accetta che nell'app sia integrato un timer software per la memorizzazione nella cache del meteo per 2 (due) ore. I dati meteorologici vengono aggiornati non più di una volta ogni 2 ore per ottimizzare il carico di rete, pertanto la temperatura visualizzata e i codici meteorologici potrebbero avere un ritardo e differire dalle effettive condizioni meteorologiche in tempo reale.\n\nPunto 4. Utilizzo a proprio rischio (Esclusione di responsabilità / \"AS IS\"): l'app, le previsioni del tempo e i suggerimenti automatici per l'abbigliamento vengono forniti \"così come sono\" esclusivamente a scopo informativo. L'utente utilizza l'app interamente a proprio rischio. Lo sviluppatore non si assume alcuna responsabilità legale, finanziaria o di altro tipo per eventuali perdite dirette o indirette, disagio, danni alla salute o alla proprietà causati da una discrepanza tra la previsione e il meteo effettivo o da un ritardo di due ore nell'aggiornamento dei dati.",
            "uz" to "1-band. Shartlarni qabul qilish: ilovadan foydalanish faqat ushbu Shartnomaga to'liq rozi bo'lgan taqdirdagina mumkin.\n\n2-band. Ob-havo ma'lumotlari manbasi va Open-Meteo litsenziyasi: barcha meteorologik ma'lumotlar uchinchi tomon xizmati Open-Meteo API (https://open-meteo.com/) tomonidan CC BY 4.0 litsenziyasi ostida taqdim etiladi. Ilova dasturchisi ob-havo stantsiyalarining egasi emas va uchinchi tomon API-sining uzluksiz ishlashiga kafolat bermaydi.\n\n3-band. Yangilanishni kechiktirish taymeri (2 soat): foydalanuvchi ilovada ob-havoni 2 (ikki) soat davomida keshlash uchun dasturiy taymer o'rnatilganligi haqida xabardor qilingan va bunga rozi. Tarmoq yuklanishini optimallashtirish uchun ob-havo ma'lumotlari har 2 soatda bir martadan ko'p yangilanmaydi, shuning uchun ko'rsatilgan harorat va ob-havo kodlari kechikishi va real vaqtdagi haqiqiy ob-havo sharoitlaridan farq qilishi mumkin.\n\n4-band. O'z tavakkalchiligingiz bilan foydalanish (Javobgarlikdan voz kechish / \"BORICHA\"): ilova, ob-havo prognozlari va avtomatik kiyim-kechak bo'yicha maslahatlar faqat ma'lumot olish maqsadida \"boricha\" taqdim etiladi. Foydalanuvchi ilovadan to'liq o'z tavakkalchiligi bilan foydalanadi. Dasturchi prognoz va haqiqiy ob-havo o'rtasidagi nomuvofiqlik yoki ma'lumotlarni yangilashdagi ikki soatlik kechikish tufayli yuzaga kelgan har qanday to'g'ridan-to'g'ri yoki bilvosita yo'qotishlar, noqulayliklar, sog'liq yoki mol-mulkka etkazilgan zarar uchun hech qanday huquqiy, moliyaviy yoki boshqa javobgarlikni o'z zimmasiga olmaydi.",
            "tr" to "Madde 1. Koşulların kabulü: uygulamanın kullanımı ancak bu Sözleşmenin tamamen kabul edilmesiyle mümkündür.\n\nMadde 2. Hava durumu veri kaynağı ve Open-Meteo lisansı: tüm meteorolojik veriler CC BY 4.0 lisansı altında bir üçüncü taraf hizmeti Open-Meteo API (https://open-meteo.com/) tarafından sağlanmaktadır. Uygulama geliştiricisi hava durumu istasyonlarının sahibi değildir ve üçüncü taraf API'sinin kesintisiz çalışmasını garanti etmez.\n\nMadde 3. Güncelleme geciktirme zamanlayıcısı (2 saat): kullanıcı, uygulamada hava durumunu 2 (iki) saat önbelleğe almak için yerleşik bir yazılım zamanlayıcısı olduğu konusunda bilgilendirilir ve bunu kabul eder. Hava durumu verileri, ağ yükünü optimize etmek için 2 saatte bir defadan fazla güncellenmez, bu nedenle görüntülenen sıcaklık ve hava durumu kodlarında bir gecikme olabilir ve gerçek zamanlı fiili hava koşullarından farklı olabilir.\n\nMadde 4. Riski size ait olmak üzere kullanım (Sorumluluk Reddi / \"OLDUĞU GİBİ\"): uygulama, hava durumu tahminleri ve otomatik giyim ipuçları kesinlikle bilgi amaçlı olarak \"olduğu gibi\" sağlanmaktadır. Kullanıcı uygulamayı tamamen kendi riski altında kullanır. Geliştirici, tahmin ile gerçek hava durumu arasındaki uyuşmazlığın veya veri güncellemesindeki iki saatlik gecikmenin neden olduğu doğrudan veya dolaylı kayıplar, rahatsızlıklar, sağlık veya mülk zararları için hiçbir yasal, mali veya diğer sorumluluk taşımaz.",
            "zh-CN" to "第一条。接受条款：仅在完全同意本协议的情况下才能使用该应用程序。\n\n第二条。天气数据来源和 Open-Meteo 许可证：所有气象数据均由第三方服务 Open-Meteo API (https://open-meteo.com/) 根据 CC BY 4.0 许可证提供。应用开发者不是气象站的拥有者，不保证第三方API的不间断运行。\n\n第三条。更新延迟计时器（2 小时）：用户已收到通知并同意应用内置了一个用于缓存天气 2（两）小时的软件计时器。为优化网络负载，天气数据更新频率不超过每 2 小时一次，因此显示的温度和天气代码可能会有延迟，并且与实时的实际天气情况有所不同。\n\n第四条。使用风险自负（免责声明 /“原样”）：该应用程序、天气预报和自动服装提示仅供参考，按“原样”提供。用户使用该应用程序完全由其自行承担风险。对于因预报与实际天气不符或数据更新有两小时延迟而造成的任何直接或间接损失、不适、对健康或财产的损害，开发商不承担任何法律、财务或其他责任。",
            "fr-CA" to "Point 1. Acceptation des conditions : l'utilisation de l'application n'est possible qu'avec le plein accord à cet Accord.\n\nPoint 2. Source des données météorologiques et licence Open-Meteo : toutes les données météorologiques sont fournies par un service tiers Open-Meteo API (https://open-meteo.com/) sous la licence CC BY 4.0. Le développeur de l'application n'est pas le propriétaire des stations météorologiques et ne garantit pas le fonctionnement ininterrompu de l'API tierce.\n\nPoint 3. Minuterie de délai de mise à jour (2 heures) : l'utilisateur est informé et accepte que l'application intègre une minuterie logicielle de mise en cache de la météo pour 2 (deux) heures. Les données météorologiques ne sont pas mises à jour plus d'une fois toutes les 2 heures pour optimiser la charge du réseau, ainsi la température et les codes météorologiques affichés peuvent avoir un délai et différer des conditions météorologiques réelles en temps réel.\n\nPoint 4. Utilisation à vos propres risques (Clause de non-responsabilité / \"EN L'ÉTAT\") : l'application, les prévisions météorologiques et les conseils vestimentaires automatiques sont fournis \"en l'état\" strictement à des fins d'information. L'utilisateur utilise l'application entièrement à ses propres risques. Le développeur n'assume aucune responsabilité légale, financière ou autre pour les pertes directes ou indirectes, l'inconfort, les dommages à la santé ou aux biens causés par une inadéquation entre la prévision et la météo réelle ou un délai de deux heures dans la mise à jour des données.",
            "es" to "Punto 1. Aceptación de los términos: el uso de la aplicación solo es posible tras la total conformidad con este Acuerdo.\n\nPunto 2. Fuente de datos meteorológicos y licencia Open-Meteo: todos los datos meteorológicos son proporcionados por un servicio de terceros Open-Meteo API (https://open-meteo.com/) bajo la licencia CC BY 4.0. El desarrollador de la aplicación no es el propietario de las estaciones meteorológicas y no garantiza el funcionamiento ininterrumpido de la API de terceros.\n\nPunto 3. Temporizador de retraso de actualización (2 horas): el usuario es notificado y acepta que la aplicación tiene un temporizador de software incorporado para almacenar en caché el clima durante 2 (dos) horas. Los datos meteorológicos se actualizan no más de una vez cada 2 horas para optimizar la carga de la red, por lo que la temperatura y los códigos meteorológicos mostrados pueden tener un retraso y diferir de las condiciones climáticas reales en tiempo real.\n\nPunto 4. Uso bajo su propio riesgo (Descargo de responsabilidad / \"TAL CUAL\"): la aplicación, los pronósticos del tiempo y los consejos automáticos de ropa se proporcionan \"tal cual\" estrictamente con fines informativos. El usuario utiliza la aplicación bajo su propio riesgo. El desarrollador no asume ninguna responsabilidad legal, financiera o de otro tipo por pérdidas directas o indirectas, molestias, daños a la salud o la propiedad causados por un desajuste entre el pronóstico y el clima real o un retraso de dos horas en la actualización de los datos.",
            "ka" to "პუნქტი 1. პირობების მიღება: აპლიკაციის გამოყენება შესაძლებელია მხოლოდ ამ შეთანხმების სრული თანხმობით.\n\nპუნქტი 2. ამინდის მონაცემების წყარო და Open-Meteo-ს ლიცენზია: ყველა მეტეოროლოგიური მონაცემი მოწოდებულია მესამე მხარის სერვისის Open-Meteo API (https://open-meteo.com/) მიერ CC BY 4.0 ლიცენზიით. აპლიკაციის დეველოპერი არ არის ამინდის სადგურების მფლობელი და არ იძლევა მესამე მხარის API-ს უწყვეტი მუშაობის გარანტიას.\n\nპუნქტი 3. განახლების შეფერხების ტაიმერი (2 საათი): მომხმარებელი ინფორმირებულია და ეთანხმება, რომ აპლიკაციას აქვს ჩაშენებული პროგრამული ტაიმერი ამინდის ქეშირებისთვის 2 (ორი) საათის განმავლობაში. ამინდის მონაცემები ახლდება არა უმეტეს 2 საათში ერთხელ ქსელის დატვირთვის ოპტიმიზაციისთვის, ამიტომ ნაჩვენები ტემპერატურა და ამინდის კოდები შეიძლება შეფერხდეს და განსხვავდებოდეს რეალური ამინდის პირობებისგან რეალურ დროში.\n\nპუნქტი 4. გამოიყენეთ თქვენივე რისკით (პასუხისმგებლობის შეზღუდვა / \"როგორც არის\"): აპლიკაცია, ამინდის პროგნოზი და ტანსაცმლის ავტომატური რჩევები მოწოდებულია \"როგორც არის\" მკაცრად საინფორმაციო მიზნებისთვის. მომხმარებელი იყენებს აპლიკაციას მთლიანად საკუთარი რისკის ქვეშ. დეველოპერი არ იღებს არანაირ იურიდიულ, ფინანსურ ან სხვა პასუხისმგებლობას ნებისმიერ პირდაპირ ან ირიბ ზარალზე, დისკომფორტზე, ჯანმრთელობისთვის ან ქონების ზიანზე, რომელიც გამოწვეულია პროგნოზსა და რეალურ ამინდს შორის შეუსაბამობით ან მონაცემების განახლების ორსაათიანი დაგვიანებით.",
            "vi" to "Điểm 1. Chấp nhận các điều khoản: việc sử dụng ứng dụng chỉ có thể thực hiện được khi hoàn toàn đồng ý với Thỏa thuận này.\n\nĐiểm 2. Nguồn dữ liệu thời tiết và giấy phép Open-Meteo: tất cả dữ liệu khí tượng được cung cấp bởi dịch vụ của bên thứ ba Open-Meteo API (https://open-meteo.com/) theo giấy phép CC BY 4.0. Nhà phát triển ứng dụng không phải là chủ sở hữu của các trạm thời tiết và không đảm bảo hoạt động không bị gián đoạn của API bên thứ ba.\n\nĐiểm 3. Bộ hẹn giờ hoãn cập nhật (2 giờ): người dùng được thông báo và đồng ý rằng ứng dụng có bộ hẹn giờ phần mềm tích hợp để lưu vào bộ nhớ cache thời tiết trong 2 (hai) giờ. Dữ liệu thời tiết được cập nhật không quá 2 giờ một lần để tối ưu hóa tải mạng, vì vậy nhiệt độ và mã thời tiết được hiển thị có thể bị chậm trễ và khác với điều kiện thời tiết thực tế trong thời gian thực.\n\nĐiểm 4. Rủi ro do bạn tự chịu (Tuyên bố từ chối trách nhiệm / \"NGUYÊN TRẠNG\"): ứng dụng, dự báo thời tiết và các mẹo trang phục tự động được cung cấp trên cơ sở \"nguyên trạng\" hoàn toàn cho mục đích thông tin. Người dùng sử dụng ứng dụng hoàn toàn tự chịu rủi ro. Nhà phát triển không chịu trách nhiệm pháp lý, tài chính hoặc trách nhiệm khác đối với bất kỳ tổn thất trực tiếp hoặc gián tiếp, sự khó chịu, tổn hại đến sức khỏe hoặc tài sản do sự không khớp giữa dự báo và thời tiết thực tế hoặc sự chậm trễ hai giờ trong việc cập nhật dữ liệu."
        ),
        "terms_checkbox" to mapOf(
            "ru" to "Я полностью согласен(на) с условиями Пользовательского соглашения",
            "kk" to "Мен Пайдаланушы келісімінің шарттарымен толық келісемін",
            "en" to "I fully agree with the terms of the User Agreement",
            "pt" to "Concordo plenamente com os termos do Acordo do Usuário",
            "de" to "Ich stimme den Bedingungen der Nutzungsvereinbarung voll und ganz zu",
            "fr" to "Je suis entièrement d'accord avec les termes de l'accord d'utilisation",
            "it" to "Sono pienamente d'accordo con i termini dell'Accordo per gli utenti",
            "uz" to "Men Foydalanuvchi shartnomasi shartlariga to'liq roziman",
            "tr" to "Kullanıcı Sözleşmesinin şartlarını tamamen kabul ediyorum",
            "zh-CN" to "我完全同意用户协议的条款",
            "fr-CA" to "Je suis entièrement d'accord avec les termes de l'accord d'utilisation",
            "es" to "Estoy totalmente de acuerdo con los términos del Acuerdo de usuario",
            "ka" to "სრულად ვეთანხმები მომხმარებლის შეთანხმების პირობებს",
            "vi" to "Tôi hoàn toàn đồng ý với các điều khoản của Thỏa thuận người dùng"
        ),
        "btn_accept" to mapOf(
            "ru" to "Принять и продолжить",
            "kk" to "Қабылдау және жалғастыру",
            "en" to "Accept and continue",
            "pt" to "Aceitar e continuar",
            "de" to "Akzeptieren und fortfahren",
            "fr" to "Accepter et continuer",
            "it" to "Accetta e continua",
            "uz" to "Qabul qilish va davom etish",
            "tr" to "Kabul Et ve Devam Et",
            "zh-CN" to "接受并继续",
            "fr-CA" to "Accepter et continuer",
            "es" to "Aceptar y continuar",
            "ka" to "მიღება და გაგრძელება",
            "vi" to "Chấp nhận và tiếp tục"
        )
    )

    return texts[key]?.get(lang) ?: texts[key]?.get("ru") ?: key
}

@Composable
fun LegalFlowScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(1) }
    var isAgreed by remember(step) { mutableStateOf(false) }

    // Звездный фон
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1022))
    ) {
        val random = Random(42)
        for (i in 0..100) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val radius = random.nextFloat() * 3f + 1f
            val alpha = random.nextFloat() * 0.5f + 0.5f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LanguageSelectorButton(containerColor = Color(0xFFB548C6))
        Spacer(modifier = Modifier.height(16.dp))

        if (step == 1) {
            // ЭКРАН 1: Политика конфиденциальности
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(2.dp, Color(0xFF9575CD), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B35).copy(alpha = 0.88f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        androidx.compose.material3.Text(
                            text = getTranslatedText("policy_title"),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        androidx.compose.material3.Text(
                            text = getTranslatedText("policy_text"),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(260.dp)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.human_person))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        speed = 1.45f
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .requiredWidth(165.dp)
                            .requiredHeight(280.dp)
                            .scale(scaleX = 1.15f, scaleY = 1.35f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAgreed = !isAgreed }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF9575CD),
                        uncheckedColor = Color.White
                    )
                )
                androidx.compose.material3.Text(
                    text = getTranslatedText("policy_checkbox"),
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Button(
                onClick = { step = 2 },
                enabled = isAgreed,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9575CD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(getTranslatedText("btn_next"), color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { (context as? Activity)?.finishAndRemoveTask() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    text = getTranslatedText("btn_disagree"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // ЭКРАН 2: Пользовательское соглашение
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(2.dp, Color(0xFF9575CD), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B35).copy(alpha = 0.88f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    androidx.compose.material3.Text(
                        text = getTranslatedText("terms_title"),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    androidx.compose.material3.Text(
                        text = getTranslatedText("terms_text"),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAgreed = !isAgreed }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF9575CD),
                        uncheckedColor = Color.White
                    )
                )
                androidx.compose.material3.Text(
                    text = getTranslatedText("terms_checkbox"),
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Button(
                onClick = {
                    setLegalAccepted(context, true)
                    onFinished()
                },
                enabled = isAgreed,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9575CD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(getTranslatedText("btn_accept"), color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { (context as? Activity)?.finishAndRemoveTask() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    text = getTranslatedText("btn_disagree"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}