package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

data class AppLanguage(val code: String, val titleRu: String, val nativeTitle: String)

object AppTranslator {
    val languages = listOf(
        AppLanguage("ru", "Русский", "Русский"),
        AppLanguage("kk", "Қазақша (Казахский)", "Қазақша"),
        AppLanguage("en", "English (Английский)", "English"),
        AppLanguage("pt", "Português (Португальский)", "Português"),
        AppLanguage("de", "Deutsch (Немецкий)", "Deutsch"),
        AppLanguage("fr", "Français (Французский)", "Français"),
        AppLanguage("it", "Italiano (Итальянский)", "Italiano"),
        AppLanguage("uz", "O‘zbekcha (Узбекский)", "O‘zbekcha"),
        AppLanguage("tr", "Türkçe (Турецкий)", "Türkçe"),
        AppLanguage("zh-CN", "中文 (Китайский)", "中文"),
        AppLanguage("fr-CA", "Canadien (Канадский)", "Canadien"),
        AppLanguage("es", "Español (Испанский)", "Español"),
        AppLanguage("ka", "ქართული (Грузинский)", "ქართული"),
        AppLanguage("vi", "Tiếng Việt (Вьетнамский)", "Tiếng Việt")
    )

    var currentLanguage by mutableStateOf("ru")
    private var prefs: SharedPreferences? = null

    private val langOrder = listOf("kk", "en", "pt", "de", "fr", "it", "uz", "tr", "zh-CN", "fr-CA", "es", "ka", "vi")

    private val dictionary = mapOf(
        // БЛОК 1
        "Привет! Я твой погодный гид" to arrayOf(
            "Сәлем! Мен сенің ауа райы бойынша жолсерігіңмін", "Hi! I'm your weather guide", "Olá! Sou seu guia do tempo", "Hallo! Ich bin dein Wetterführer", "Salut ! Je suis votre guide météo", "Ciao! Sono la tua guida meteorologica", "Salom! Men sizning ob-havo bo'yicha qo'llanmangizman", "Merhaba! Ben hava durumu rehberinizim", "嗨！我是你的天气指南", "Salut! Je suis ton guide météo", "¡Hola! Soy tu guía del clima", "გამარჯობა! მე ვარ შენი ამინდის გიდი", "Chào! Tôi là hướng dẫn viên thời tiết của bạn"
        ),
        "Это приложение создано для того, чтобы погода никогда не застала тебя врасплох. Здесь ты можешь в один клик узнать температуру по своей геолокации, собрать коллекцию любимых городов мира и получить умные советы, что надеть перед выходом на улицу!" to arrayOf(
            "Бұл қолданба ауа райы сізді ешқашан күтпеген жерден ұстамауы үшін жасалған. Мұнда сіз бір рет басу арқылы геолокация бойынша температураны біле аласыз, әлемнің сүйікті қалаларының топтамасын жинай аласыз және далаға шықпас бұрын не кию керектігі туралы ақылды кеңестер ала аласыз!",
            "This app is designed so that the weather never catches you by surprise. Here you can find out the temperature by your geolocation in one click, collect a collection of your favorite cities in the world and get smart tips on what to wear before going outside!",
            "Este aplicativo foi projetado para que o clima nunca o pegue de surpresa. Aqui você pode descobrir a temperatura da sua geolocalização com um clique, colecionar suas cidades favoritas do mundo e obter dicas inteligentes sobre o que vestir antes de sair de casa!",
            "Diese App wurde entwickelt, damit dich das Wetter nie wieder überrascht. Hier kannst du mit einem Klick die Temperatur deines Standorts erfahren, eine Sammlung deiner Lieblingsstädte der Welt zusammenstellen und smarte Tipps bekommen, was du anziehen solltest, bevor du nach draußen gehst!",
            "Cette application est conçue pour que la météo ne vous prenne jamais au dépourvu. Ici, vous pouvez connaître la température de votre géolocalisation en un clic, rassembler une collection de vos villes préférées dans le monde et obtenir des conseils judicieux sur quoi porter avant de sortir !",
            "Quest'app è progettata in modo che il tempo non ti colga mai di sorpresa. Qui puoi scoprire la temperatura in base alla tua geolocalizzazione in un clic, raccogliere una collezione delle tue città preferite al mondo e ottenere consigli intelligenti su cosa indossare prima di uscire!",
            "Ushbu ilova ob-havo sizni hech qachon ajablantirmasligi uchun yaratilgan. Bu yerda siz bir marta bosish orqali geolokatsiyangiz bo'yicha haroratni bilib olishingiz, dunyodagi sevimli shaharlaringiz to'plamini to'plashingiz va ko'chaga chiqishdan oldin nima kiyish bo'yicha aqlli maslahatlar olishingiz mumkin!",
            "Bu uygulama, havanın sizi asla hazırlıksız yakalamaması için tasarlandı. Burada tek bir tıklamayla konumunuza göre sıcaklığı öğrenebilir, dünyadaki favori şehirlerinizden bir koleksiyon toplayabilir ve dışarı çıkmadan önce ne giyeceğinize dair akıllı ipuçları alabilirsiniz!",
            "这款应用程序的初衷是为了让天气永远不会让您措手不及。在这里，您可以一键了解您所在位置的温度，收集世界上您最喜欢的城市的集合，并获得关于外出前穿什么衣服的智能提示！",
            "Cette application est conçue pour que la météo ne te prenne jamais au dépourvu. Ici, tu peux connaître la température à ton emplacement en un clic, collectionner tes villes préférées dans le monde et obtenir des conseils pratiques sur quoi porter avant de sortir!",
            "Esta aplicación está diseñada para que el clima nunca te tome por sorpresa. Aquí puedes conocer la temperatura según tu geolocalización con un solo clic, crear una colección de tus ciudades favoritas del mundo y obtener consejos inteligentes sobre qué ponerte antes de salir.",
            "ეს აპლიკაცია შექმნილია იმისათვის, რომ ამინდმა არასოდეს გაგაკვირვოთ. აქ შეგიძლიათ გაიგოთ ტემპერატურა თქვენი გეოლოკაციის მიხედვით ერთი დაწკაპუნებით, შეაგროვოთ თქვენი საყვარელი ქალაქების კოლექცია მსოფლიოში და მიიღოთ ჭკვიანი რჩევები იმის შესახებ, თუ რა ჩაიცვათ გარეთ გასვლამდე!",
            "Ứng dụng này được thiết kế để thời tiết không bao giờ khiến bạn bất ngờ. Tại đây, bạn có thể tìm ra nhiệt độ theo vị trí địa lý của mình chỉ bằng một cú nhấp chuột, thu thập bộ sưu tập các thành phố yêu thích trên thế giới và nhận các mẹo thông minh về những gì nên mặc trước khi ra ngoài!"
        ),
        "Начать пользоваться" to arrayOf(
            "Қолдануды бастау", "Start using", "Começar a usar", "Beginnen zu nutzen", "Commencer à utiliser", "Inizia a usare", "Foydalanishni boshlash", "Kullanmaya başla", "开始使用", "Commencer", "Empezar a usar", "დაწყება", "Bắt đầu sử dụng"
        ),

        // БЛОК 2
        "Поиск города" to arrayOf("Қаланы іздеу", "Search city", "Procurar cidade", "Stadt suchen", "Rechercher une ville", "Cerca città", "Shaharni qidirish", "Şehir ara", "搜索城市", "Rechercher une ville", "Buscar ciudad", "ქალაქის ძებნა", "Tìm kiếm thành phố"),
        "Введите минимум 2 символа" to arrayOf("Кем дегенде 2 таңба енгізіңіз", "Enter at least 2 characters", "Insira pelo menos 2 caracteres", "Geben Sie mindestens 2 Zeichen ein", "Entrez au moins 2 caractères", "Inserisci almeno 2 caratteri", "Kamida 2 ta belgi kiriting", "En az 2 karakter girin", "输入至少2个字符", "Entrez au moins 2 caractères", "Ingrese al menos 2 caracteres", "შეიყვანეთ მინიმუმ 2 სიმბოლო", "Nhập ít nhất 2 ký tự"),
        "Результаты поиска:" to arrayOf("Іздеу нәтижелері:", "Search results:", "Resultados da busca:", "Suchergebnisse:", "Résultats de recherche :", "Risultati di ricerca:", "Qidiruv natijalari:", "Arama sonuçları:", "搜索结果：", "Résultats de la recherche :", "Resultados de la búsqueda:", "ძიების შედეგები:", "Kết quả tìm kiếm:"),
        "Повторить попытку" to arrayOf("Қайталау", "Try again", "Tentar novamente", "Erneut versuchen", "Réessayer", "Riprova", "Qayta urinish", "Tekrar dene", "重试", "Réessayer", "Intentar de nuevo", "ხელახლა ცდა", "Thử lại"),
        "Выберите в поиске вашу страну и ваш город!" to arrayOf("Іздеуде еліңізді және қалаңызды таңдаңыз!", "Select your country and city in the search!", "Selecione seu país e cidade na pesquisa!", "Wähle dein Land und deine Stadt in der Suche aus!", "Sélectionnez votre pays et votre ville dans la recherche !", "Seleziona il tuo paese e la tua città nella ricerca!", "Qidiruvda mamlakatingiz va shahringizni tanlang!", "Aramada ülkenizi ve şehrinizi seçin!", "在搜索中选择您的国家和城市！", "Sélectionne ton pays et ta ville dans la recherche !", "¡Selecciona tu país y ciudad en la búsqueda!", "აირჩიეთ თქვენი ქვეყანა და ქალაქი ძიებაში!", "Chọn quốc gia và thành phố của bạn trong tìm kiếm!"),
        "Текущее место" to arrayOf("Қазіргі орын", "Current location", "Local atual", "Aktueller Ort", "Lieu actuel", "Posizione attuale", "Joriy joylashuv", "Mevcut konum", "当前位置", "Emplacement actuel", "Ubicación actual", "მიმდინარე ადგილი", "Vị trí hiện tại"),
        "Моя локация" to arrayOf("Менің локациям", "My location", "Minha localização", "Mein Standort", "Ma position", "La mia posizione", "Mening joylashuvim", "Konumum", "我的位置", "Ma position", "Mi ubicación", "ჩემი ლოკაცია", "Vị trí của tôi"),
        "Приблизительная местность" to arrayOf("Шамамен орналасқан жер", "Approximate area", "Área aproximada", "Ungefähre Gegend", "Zone approximative", "Area approssimativa", "Taxminiy hudud", "Yaklaşık bölge", "大致区域", "Zone approximative", "Área aproximada", "მიახლოებითი ტერიტორია", "Khu vực gần đúng"),
        "Местность" to arrayOf("Жер", "Area", "Área", "Gegend", "Zone", "Area", "Hudud", "Bölge", "区域", "Région", "Área", "ადგილი", "Khu vực"),
        "Неизвестно" to arrayOf("Белгісіз", "Unknown", "Desconhecido", "Unbekannt", "Inconnu", "Sconosciuto", "Noma'lum", "Bilinmeyen", "未知", "Inconnu", "Desconocido", "უცნობია", "Không xác định"),
        "Дополнительные данные загружаются..." to arrayOf("Қосымша деректер жүктелуде...", "Additional data is loading...", "Dados adicionais estão carregando...", "Zusätzliche Daten werden geladen...", "Des données supplémentaires sont en cours de chargement...", "Caricamento dati aggiuntivi in corso...", "Qo'shimcha ma'lumotlar yuklanmoqda...", "Ek veriler yükleniyor...", "其他数据正在加载...", "Chargement de données supplémentaires...", "Cargando datos adicionales...", "დამატებითი მონაცემები იტვირთება...", "Dữ liệu bổ sung đang được tải..."),
        "Ощущается как:" to arrayOf("Сезiледi:", "Feels like:", "Sensação térmica:", "Gefühlt wie:", "Ressenti :", "Percepita come:", "Tuyuladi:", "Hissedilen:", "体感温度：", "Ressenti :", "Sensación térmica:", "იგრძნობა როგორც:", "Cảm giác như:"),
        "Подробнее" to arrayOf("Толығырақ", "More details", "Mais detalhes", "Mehr Details", "Plus de détails", "Più dettagli", "Batafsil", "Daha fazla", "更多细节", "Détails", "Más detalles", "დეტალურად", "Thêm chi tiết"),
        "Назад" to arrayOf("Артқа", "Back", "Voltar", "Zurück", "Retour", "Indietro", "Orqaga", "Geri", "返回", "Retour", "Atrás", "უკან", "Quay lại"),
        "Фото профиля" to arrayOf("Профиль суреті", "Profile photo", "Foto de perfil", "Profilfoto", "Photo de profil", "Foto del profilo", "Profil rasmi", "Profil fotoğrafı", "个人资料照片", "Photo de profil", "Foto de perfil", "პროფილის ფოტო", "Ảnh hồ sơ"),
        "Выберите действие с аватаркой" to arrayOf("Аватармен әрекетті таңдаңыз", "Select an action for the avatar", "Selecione uma ação para o avatar", "Aktion für den Avatar auswählen", "Sélectionnez une action pour l'avatar", "Seleziona un'azione per l'avatar", "Avatar uchun amalni tanlang", "Avatar için bir işlem seçin", "选择头像操作", "Choisissez une action pour l'avatar", "Seleccione una acción para el avatar", "აირჩიეთ მოქმედება ავატარისთვის", "Chọn thao tác cho ảnh đại diện"),
        "Изменить" to arrayOf("Өзгерту", "Change", "Alterar", "Ändern", "Changer", "Modifica", "O'zgartirish", "Değiştir", "更改", "Modifier", "Cambiar", "შეცვლა", "Thay đổi"),
        "Удалить" to arrayOf("Жою", "Delete", "Excluir", "Löschen", "Supprimer", "Elimina", "O'chirish", "Sil", "删除", "Supprimer", "Eliminar", "წაშლა", "Xóa"),
        "Отмена" to arrayOf("Болдырмау", "Cancel", "Cancelar", "Abbrechen", "Annuler", "Annulla", "Bekor qilish", "İptal", "取消", "Annuler", "Cancelar", "გაუქმება", "Hủy"),
        "Страна" to arrayOf("Ел", "Country", "País", "Land", "Pays", "Paese", "Mamlakat", "Ülke", "国家", "Pays", "País", "ქვეყანა", "Quốc gia"),
        "Страна не может быть пустой" to arrayOf("Ел бос болмауы керек", "Country cannot be empty", "O país não pode estar vazio", "Land darf nicht leer sein", "Le pays ne peut pas être vide", "Il paese non può essere vuoto", "Mamlakat bo'sh bo'lishi mumkin emas", "Ülke boş olamaz", "国家不能为空", "Le pays ne peut pas être vide", "El país no puede estar vacío", "ქვეყანა არ შეიძლება იყოს ცარიელი", "Quốc gia không được để trống"),
        "Любимый город" to arrayOf("Сүйікті қала", "Favorite city", "Cidade favorita", "Lieblingsstadt", "Ville préférée", "Città preferita", "Sevimli shahar", "Favori şehir", "最喜欢的城市", "Ville préférée", "Ciudad favorita", "საყვარელი ქალაქი", "Thành phố yêu thích"),
        "Город не может быть пустым" to arrayOf("Қала бос болмауы керек", "City cannot be empty", "A cidade não pode estar vazia", "Stadt darf nicht leer sein", "La ville ne peut pas être vide", "La città non può essere vuota", "Shahar bo'sh bo'lishi mumkin emas", "Şehir boş olamaz", "城市不能为空", "La ville ne peut pas être vide", "La ciudad no puede estar vacía", "ქალაქი არ შეიძლება იყოს ცარიელი", "Thành phố không được để trống"),
        "Сохранить настройки" to arrayOf("Параметрлерді сақтау", "Save settings", "Salvar configurações", "Einstellungen speichern", "Enregistrer les paramètres", "Salva impostazioni", "Sozlamalarni saqlash", "Ayarları kaydet", "保存设置", "Enregistrer", "Guardar ajustes", "პარამეტრების შენახვა", "Lưu cài đặt"),
        "Вернуться назад" to arrayOf("Артқа оралу", "Go back", "Voltar", "Zurückkehren", "Retourner", "Torna indietro", "Orqaga qaytish", "Geri dön", "退回", "Revenir", "Volver", "უკან დაბრუნება", "Trở lại"),
        "Язык" to arrayOf("Тіл", "Language", "Idioma", "Sprache", "Langue", "Lingua", "Til", "Dil", "语言", "Langue", "Idioma", "ენა", "Ngôn ngữ"),
        "Выберите язык" to arrayOf("Тілді таңдаңыз", "Choose a language", "Escolha um idioma", "Sprache auswählen", "Choisissez une langue", "Scegli una lingua", "Tilni tanlang", "Bir dil seçin", "选择语言", "Choisir une langue", "Elige un idioma", "აირჩიეთ ენა", "Chọn một ngôn ngữ"),
        "Тёмная тема" to arrayOf("Қараңғы тақырып", "Dark theme", "Tema escuro", "Dunkles Design", "Thème sombre", "Tema scuro", "Qorong'i mavzu", "Karanlık tema", "深色主题", "Thème sombre", "Tema oscuro", "ბნელი თემა", "Chủ đề tối"),
        "Выйти из приложения" to arrayOf("Қолданбадан шығу", "Exit the application", "Sair do aplicativo", "App verlassen", "Quitter l'application", "Esci dall'app", "Ilovadan chiqish", "Uygulamadan çık", "退出应用", "Quitter l'application", "Salir de la aplicación", "აპლიკაციიდან გასვლა", "Thoát ứng dụng"),
        "Выход" to arrayOf("Шығу", "Exit", "Sair", "Beenden", "Quitter", "Uscita", "Chiqish", "Çıkış", "退出", "Quitter", "Salir", "გასვლა", "Thoát"),
        "Вы точно хотите выйти?" to arrayOf("Шығуды шынымен қалайсыз ба?", "Are you sure you want to exit?", "Tem certeza de que deseja sair?", "Bist du sicher, dass du beenden möchtest?", "Voulez-vous vraiment quitter ?", "Sei sicuro di voler uscire?", "Rostdan ham chiqmoqchimisiz?", "Çıkmak istediğinize emin misiniz?", "你确定要退出吗？", "Veux-tu vraiment quitter ?", "¿Seguro que quieres salir?", "დარწმუნებული ხართ, რომ გსურთ გასვლა?", "Bạn có chắc chắn muốn thoát không?"),
        "Нет" to arrayOf("Жоқ", "No", "Não", "Nein", "Non", "No", "Yo'q", "Hayır", "不", "Non", "No", "არა", "Không"),
        "Да" to arrayOf("Иә", "Yes", "Sim", "Ja", "Oui", "Sì", "Ha", "Evet", "是", "Oui", "Sí", "დიახ", "Có"),
        "Геолокация запрещена" to arrayOf("Геолокацияға тыйым салынған", "Geolocation denied", "Geolocalização negada", "Geolokalisierung verweigert", "Géolocalisation refusée", "Geolocalizzazione negata", "Geolokatsiya ruxsat etilmagan", "Konum izni reddedildi", "地理位置被拒绝", "Géolocalisation refusée", "Geolocalización denegada", "გეოლოკაცია აკრძალულია", "Vị trí địa lý bị từ chối"),
        "Ошибка получения локации" to arrayOf("Орналасқан жерді алу қатесі", "Error getting location", "Erro ao obter localização", "Fehler beim Abrufen des Standorts", "Erreur lors de l'obtention de la localisation", "Errore nell'ottenere la posizione", "Joylashuvni aniqlashda xatolik", "Konum alınırken hata oluştu", "获取位置错误", "Erreur de localisation", "Error al obtener la ubicación", "ლოკაციის მიღების შეცდომა", "Lỗi lấy vị trí"),
        "Сбой систем служб геолокации" to arrayOf("Геолокация қызметтері жүйесінің ақаулығы", "Geolocation services system failure", "Falha no sistema de serviços de geolocalização", "Systemausfall der Geolokalisierungsdienste", "Échec du système des services de géolocalisation", "Guasto del sistema dei servizi di geolocalizzazione", "Geolokatsiya xizmatlari tizimidagi nosozlik", "Konum servisleri sistem hatası", "地理位置服务系统故障", "Échec des services de localisation", "Fallo del sistema de servicios de geolocalización", "გეოლოკაციის სერვისების სისტემის უკმარისობა", "Lỗi hệ thống dịch vụ định vị"),
        "Вы уже добавили этот город!" to arrayOf("Сіз бұл қаланы қосып қойдыңыз!", "You have already added this city!", "Você já adicionou esta cidade!", "Du hast diese Stadt bereits hinzugefügt!", "Vous avez déjà ajouté cette ville !", "Hai già aggiunto questa città!", "Siz bu shaharni qo'shib bo'lgansiz!", "Bu şehri zaten eklediniz!", "您已经添加了这个城市！", "Tu as déjà ajouté cette ville !", "¡Ya has añadido esta ciudad!", "თქვენ უკვე დაამატეთ ეს ქალაქი!", "Bạn đã thêm thành phố này rồi!"),
        "Для выбора аватарки нужен доступ к галерее" to arrayOf("Аватарды таңдау үшін галереяға кіру қажет", "Need gallery access to select avatar", "É necessário acesso à galeria para selecionar o avatar", "Galeriezugriff wird für die Avatar-Auswahl benötigt", "Accès à la galerie requis pour sélectionner un avatar", "Accesso alla galleria necessario per selezionare l'avatar", "Avatarni tanlash uchun galereyaga kirish ruxsati kerak", "Avatar seçmek için galeri erişimi gerekiyor", "需要图库访问权限才能选择头像", "Accès à la galerie requis", "Necesita acceso a la galería para seleccionar avatar", "ავატარის ასარჩევად გალერეაზე წვდომაა საჭირო", "Cần quyền truy cập thư viện để chọn ảnh đại diện"),
        "Настройки сохранены!" to arrayOf("Параметрлер сақталды!", "Settings saved!", "Configurações salvas!", "Einstellungen gespeichert!", "Paramètres enregistrés !", "Impostazioni salvate!", "Sozlamalar saqlandi!", "Ayarlar kaydedildi!", "设置已保存！", "Paramètres sauvegardés !", "¡Ajustes guardados!", "პარამეტრები შენახულია!", "Cài đặt đã được lưu!"),

        // БЛОК 3: Страны
        "Казахстан" to arrayOf("Қазақстан", "Kazakhstan", "Cazaquistão", "Kasachstan", "Kazakhstan", "Kazakistan", "Qozog'iston", "Kazakistan", "哈萨克斯坦", "Kazakhstan", "Kazajistán", "ყაზახეთი", "Kazakhstan"),
        "Россия" to arrayOf("Ресей", "Russia", "Rússia", "Russland", "Russie", "Russia", "Rossiya", "Rusya", "俄罗斯", "Russie", "Rusia", "რუსეთი", "Nga"),
        "Узбекистан" to arrayOf("Өзбекстан", "Uzbekistan", "Uzbequistão", "Usbekistan", "Ouzbékistan", "Uzbekistan", "O'zbekiston", "Özbekistan", "乌兹别克斯坦", "Ouzbékistan", "Uzbekistán", "უზბეკეთი", "Uzbekistan"),
        "ОАЭ" to arrayOf("БАӘ", "UAE", "EAU", "VAE", "ÉAU", "EAU", "BAA", "BAE", "阿联酋", "ÉAU", "EAU", "არაბთა გაერთიანებული საამიროები", "UAE"),
        "США" to arrayOf("АҚШ", "USA", "EUA", "USA", "États-Unis", "Stati Uniti", "AQSH", "ABD", "美国", "États-Unis", "EE. UU.", "აშშ", "Mỹ"),
        "Турция" to arrayOf("Түркия", "Turkey", "Turquia", "Türkei", "Turquie", "Turchia", "Turkiya", "Türkiye", "土耳其", "Turquie", "Turquía", "თურქეთი", "Thổ Nhĩ Kỳ"),
        "Германия" to arrayOf("Германия", "Germany", "Alemanha", "Deutschland", "Allemagne", "Germania", "Germaniya", "Almanya", "德国", "Allemagne", "Alemania", "გერმანია", "Đức"),
        "Китай" to arrayOf("Қытай", "China", "China", "China", "Chine", "Cina", "Xitoy", "Çin", "中国", "Chine", "China", "ჩინეთი", "Trung Quốc"),
        "Австралия" to arrayOf("Австралия", "Australia", "Austrália", "Australien", "Australie", "Australia", "Avstraliya", "Avustralya", "澳大利亚", "Australie", "Australia", "ავსტრალია", "Úc"),
        "Испания" to arrayOf("Испания", "Spain", "Espanha", "Spanien", "Espagne", "Spagna", "Ispaniya", "İspanya", "西班牙", "Espagne", "España", "ესპანეთი", "Tây Ban Nha"),
        "Бразилия" to arrayOf("Бразилия", "Brazil", "Brasil", "Brasilien", "Brésil", "Brasile", "Braziliya", "Brezilya", "巴西", "Brésil", "Brasil", "ბრაზილია", "Brazil"),
        "Канада" to arrayOf("Канада", "Canada", "Canadá", "Kanada", "Canada", "Canada", "Kanada", "Kanada", "加拿大", "Canada", "Canadá", "კანადა", "Canada"),
        "Италия" to arrayOf("Италия", "Italy", "Itália", "Italien", "Italie", "Italia", "Italiya", "İtalya", "意大利", "Italie", "Italia", "იტალია", "Ý"),
        "Вьетнам" to arrayOf("Вьетнам", "Vietnam", "Vietnã", "Vietnam", "Vietnam", "Vietnam", "Vyetnam", "Vietnam", "越南", "Vietnam", "Vietnam", "ვიეტნამი", "Việt Nam"),
        "Грузия" to arrayOf("Грузия", "Georgia", "Geórgia", "Georgien", "Géorgie", "Georgia", "Gruziya", "Gürcistan", "格鲁吉亚", "Géorgie", "Georgia", "საქართველო", "Georgia"),
        "Таиланд" to arrayOf("Тайланд", "Thailand", "Tailândia", "Thailand", "Thaïlande", "Tailandia", "Tailand", "Tayland", "泰国", "Thaïlande", "Tailandia", "ტაილანდი", "Thái Lan"),

        // БЛОК 3: Города
        "Астана" to arrayOf("Астана", "Astana", "Astana", "Astana", "Astana", "Astana", "Astana", "Astana", "阿斯塔纳", "Astana", "Astaná", "ასტანა", "Astana"),
        "Алматы" to arrayOf("Алматы", "Almaty", "Almaty", "Almaty", "Almaty", "Almaty", "Olmaota", "Almatı", "阿拉木图", "Almaty", "Almatý", "ალმათი", "Almaty"),
        "Шымкент" to arrayOf("Шымкент", "Shymkent", "Shymkent", "Schymkent", "Chymkent", "Shymkent", "Chimkent", "Çimkent", "奇姆肯特", "Chymkent", "Shymkent", "შიმკენტი", "Shymkent"),
        "Актобе" to arrayOf("Ақтөбе", "Aktobe", "Aktobe", "Aqtöbe", "Aqtöbe", "Aktobe", "Aqto'be", "Aktöbe", "阿克托别", "Aqtöbe", "Aktobe", "აქტობე", "Aktobe"),
        "Москва" to arrayOf("Мәскеу", "Moscow", "Moscou", "Moskau", "Moscou", "Mosca", "Moskva", "Moskova", "莫斯科", "Moscou", "Moscú", "მოსკოვი", "Moscow"),
        "Санкт-Петербург" to arrayOf("Санкт-Петербург", "St. Petersburg", "São Petersburgo", "St. Petersburg", "Saint-Pétersbourg", "San Pietroburgo", "Sankt-Peterburg", "St. Petersburg", "圣彼得堡", "Saint-Pétersbourg", "San Petersburgo", "სანქტ-პეტერბურგი", "St. Petersburg"),
        "Новосибирск" to arrayOf("Новосібір", "Novosibirsk", "Novosibirsk", "Nowosibirsk", "Novossibirsk", "Novosibirsk", "Novosibirsk", "Novosibirsk", "新西伯利亚", "Novossibirsk", "Novosibirsk", "ნოვოსიბირსკი", "Novosibirsk"),
        "Ташкент" to arrayOf("Ташкент", "Tashkent", "Tashkent", "Taschkent", "Tachkent", "Tashkent", "Toshkent", "Taşkent", "塔什干", "Tachkent", "Taskent", "ტაშკენტი", "Tashkent"),
        "Самарканд" to arrayOf("Самарқанд", "Samarkand", "Samarcanda", "Samarkand", "Samarcande", "Samarcanda", "Samarqand", "Semerkant", "撒马尔罕", "Samarcande", "Samarcanda", "სამარყანდი", "Samarkand"),
        "Дубай" to arrayOf("Дубай", "Dubai", "Dubai", "Dubai", "Dubaï", "Dubai", "Dubay", "Dubai", "迪拜", "Dubaï", "Dubái", "დუბაი", "Dubai"),
        "Абу-Даби" to arrayOf("Абу-Даби", "Abu Dhabi", "Abu Dhabi", "Abu Dhabi", "Abou Dabi", "Abu Dhabi", "Abu-Dabi", "Abu Dabi", "阿布扎比", "Abou Dabi", "Abu Dabi", "აბუ-დაბი", "Abu Dhabi"),
        "Нью-Йорк" to arrayOf("Нью-Йорк", "New York", "Nova York", "New York", "New York", "New York", "Nyu-York", "New York", "纽约", "New York", "Nueva York", "ნიუ-იორკი", "New York"),
        "Лос-Анджелес" to arrayOf("Лос-Анджелес", "Los Angeles", "Los Angeles", "Los Angeles", "Los Angeles", "Los Angeles", "Los-Anjeles", "Los Angeles", "洛杉矶", "Los Angeles", "Los Ángeles", "ლოს-ანჯელესი", "Los Angeles"),
        "Чикаго" to arrayOf("Чикаго", "Chicago", "Chicago", "Chicago", "Chicago", "Chicago", "Chikago", "Şikago", "芝加哥", "Chicago", "Chicago", "ჩიკაგო", "Chicago"),
        "Стамбул" to arrayOf("Ыстамбұл", "Istanbul", "Istambul", "Istanbul", "Istanbul", "Istanbul", "Istanbul", "İstanbul", "伊斯坦布尔", "Istanbul", "Estambul", "სტამბოლი", "Istanbul"),
        "Анкара" to arrayOf("Анкара", "Ankara", "Ancara", "Ankara", "Ankara", "Ankara", "Anqara", "Ankara", "安卡拉", "Ankara", "Ankara", "ანკარა", "Ankara"),
        "Анталья" to arrayOf("Анталия", "Antalya", "Antalya", "Antalya", "Antalya", "Antalya", "Antaliya", "Antalya", "安塔利亚", "Antalya", "Antalya", "ანტალია", "Antalya"),
        "Берлин" to arrayOf("Берлин", "Berlin", "Berlim", "Berlin", "Berlin", "Berlino", "Berlin", "Berlin", "柏林", "Berlin", "Berlín", "ბერლინი", "Berlin"),
        "Мюнхен" to arrayOf("Мюнхен", "Munich", "Munique", "München", "Munich", "Monaco", "Myunxen", "Münih", "慕尼黑", "Munich", "Múnich", "მიუნხენი", "Munich"),
        "Пекин" to arrayOf("Бейжің", "Beijing", "Pequim", "Peking", "Pékin", "Pechino", "Pekin", "Pekin", "北京", "Pékin", "Pekín", "პეკინი", "Bắc Kinh"),
        "Шанхай" to arrayOf("Шанхай", "Shanghai", "Xangai", "Shanghai", "Shanghai", "Shanghai", "Shanxay", "Şanghay", "上海", "Shanghai", "Shanghái", "შანხაი", "Thượng Hải"),
        "Гуанчжоу" to arrayOf("Гуанчжоу", "Guangzhou", "Guangzhou", "Guangzhou", "Guangzhou", "Canton", "Guanchjou", "Guangzhou", "广州", "Guangzhou", "Cantón", "გუანჯოუ", "Quảng Châu"),
        "Сидней" to arrayOf("Сидней", "Sydney", "Sydney", "Sydney", "Sydney", "Sydney", "Sidney", "Sidney", "悉尼", "Sydney", "Sídney", "სიდნეი", "Sydney"),
        "Мельбурн" to arrayOf("Мельбурн", "Melbourne", "Melbourne", "Melbourne", "Melbourne", "Melbourne", "Melburn", "Melbourne", "墨尔本", "Melbourne", "Melbourne", "მელბურნი", "Melbourne"),
        "Мадрид" to arrayOf("Мадрид", "Madrid", "Madri", "Madrid", "Madrid", "Madrid", "Madrid", "Madrid", "马德里", "Madrid", "Madrid", "მადრიდი", "Madrid"),
        "Барселона" to arrayOf("Барселона", "Barcelona", "Barcelona", "Barcelona", "Barcelone", "Barcellona", "Barselona", "Barselona", "巴塞罗那", "Barcelone", "Barcelona", "ბარსელონა", "Barcelona"),
        "Рио-де-Жанейро" to arrayOf("Рио-де-Жанейро", "Rio de Janeiro", "Rio de Janeiro", "Rio de Janeiro", "Rio de Janeiro", "Rio de Janeiro", "Rio-de-Janeyro", "Rio de Janeiro", "里约热内卢", "Rio de Janeiro", "Río de Janeiro", "რიო-დე-ჟანეირო", "Rio de Janeiro"),
        "Сан-Паулу" to arrayOf("Сан-Паулу", "São Paulo", "São Paulo", "São Paulo", "São Paulo", "San Paolo", "San-Paulu", "São Paulo", "圣保罗", "São Paulo", "São Paulo", "სან-პაულუ", "São Paulo"),
        "Торонто" to arrayOf("Торонто", "Toronto", "Toronto", "Toronto", "Toronto", "Toronto", "Toronto", "Toronto", "多伦多", "Toronto", "Toronto", "ტორონტო", "Toronto"),
        "Ванкувер" to arrayOf("Ванкувер", "Vancouver", "Vancouver", "Vancouver", "Vancouver", "Vancouver", "Vankuver", "Vancouver", "温哥华", "Vancouver", "Vancouver", "ვანკუვერი", "Vancouver"),
        "Рим" to arrayOf("Рим", "Rome", "Roma", "Rom", "Rome", "Roma", "Rim", "Roma", "罗马", "Rome", "Roma", "რომი", "Rome"),
        "Милан" to arrayOf("Милан", "Milan", "Milão", "Mailand", "Milan", "Milano", "Milan", "Milano", "米兰", "Milan", "Milán", "მილანი", "Milan"),
        "Ханой" to arrayOf("Ханой", "Hanoi", "Hanói", "Hanoi", "Hanoï", "Hanoi", "Xanoy", "Hanoi", "河内", "Hanoï", "Hanói", "ჰანოი", "Hà Nội"),
        "Хошимин" to arrayOf("Хошимин", "Ho Chi Minh City", "Cidade de Ho Chi Minh", "Ho-Chi-Minh-Stadt", "Hô Chi Minh-Ville", "Ho Chi Minh", "Xoshimin", "Ho Chi Minh Kenti", "胡志明市", "Hô Chi Minh-Ville", "Ciudad Ho Chi Minh", "ჰოშიმინი", "Thành phố Hồ Chí Minh"),
        "Тбилиси" to arrayOf("Тбилиси", "Tbilisi", "Tbilisi", "Tiflis", "Tbilissi", "Tbilisi", "Tbilisi", "Tiflis", "第比利斯", "Tbilissi", "Tiflis", "თბილისი", "Tbilisi"),
        "Батуми" to arrayOf("Батуми", "Batumi", "Batumi", "Batumi", "Batoumi", "Batumi", "Batumi", "Batum", "巴统", "Batoumi", "Batumi", "ბათუმი", "Batumi"),
        "Бангкок" to arrayOf("Бангкок", "Bangkok", "Bangcoc", "Bangkok", "Bangkok", "Bangkok", "Bangkok", "Bangkok", "曼谷", "Bangkok", "Bangkok", "ბანგკოკი", "Bangkok"),
        "Пхукет" to arrayOf("Пхукет", "Phuket", "Phuket", "Phuket", "Phuket", "Phuket", "Pxuket", "Phuket", "普吉岛", "Phuket", "Phuket", "ფხუკეტი", "Phuket"),

        // БЛОК 4: Советы по одежде
        "На улице ожидается гроза и сильный ветер — лучше отложить долгие прогулки и захватить надёжный дождевик." to arrayOf(
            "Далада найзағай мен қатты жел күтілуде — ұзақ серуендеуді кейінге қалдырып, сенімді пальто алған дұрыс.",
            "Thunderstorms and strong winds are expected outside — it's better to postpone long walks and grab a reliable raincoat.",
            "Esperam-se trovoadas e ventos fortes lá fora — é melhor adiar longas caminhadas e pegar uma capa de chuva confiável.",
            "Draußen werden Gewitter und starke Winde erwartet — es ist besser, lange Spaziergänge zu verschieben und einen zuverlässigen Regenmantel mitzunehmen.",
            "Des orages et des vents violents sont attendus à l'extérieur - il vaut mieux reporter les longues promenades et prendre un imperméable fiable.",
            "All'esterno sono previsti temporali e forti venti — è meglio rimandare le lunghe passeggiate e prendere un impermeabile affidabile.",
            "Ko'chada momaqaldiroq va kuchli shamol kutilmoqda — uzoq yurishni qoldirib, ishonchli yomg'ir paltosini olib borgan ma'qul.",
            "Dışarıda gökgürültülü fırtına ve şiddetli rüzgar bekleniyor — uzun yürüyüşleri ertelemek ve güvenilir bir yağmurluk almak daha iyi.",
            "外面预计会有雷雨和强风——最好推迟长途散步，拿一件可靠的雨衣。",
            "Des orages et des vents forts sont prévus — il vaut mieux reporter les longues marches et prendre un bon imperméable.",
            "Se esperan tormentas eléctricas y vientos fuertes afuera — es mejor posponer caminatas largas y llevar un impermeable confiable.",
            "გარეთ მოსალოდნელია ჭექა-ქუხილი და ძლიერი ქარი — უმჯობესია გადადოთ ხანგრძლივი სეირნობა და აიღოთ საიმედო საწვიმარი.",
            "Bên ngoài dự kiến có giông bão và gió mạnh — tốt nhất nên hoãn các chuyến đi bộ dài và mang theo áo mưa đáng tin cậy."
        ),
        "Небо хмурится, возможна гроза. Держите зонт под рукой и выбирайте закрытую непромокаемую обувь." to arrayOf(
            "Аспан бұлтты, найзағай болуы мүмкін. Қолшатырды жақын ұстап, жабық су өтпейтін аяқ киімді таңдаңыз.",
            "The sky is frowning, a thunderstorm is possible. Keep an umbrella handy and choose closed, waterproof shoes.",
            "O céu está carrancudo, uma tempestade é possível. Mantenha um guarda-chuva à mão e escolha sapatos fechados e impermeáveis.",
            "Der Himmel ist bewölkt, ein Gewitter ist möglich. Halten Sie einen Regenschirm griffbereit und wählen Sie geschlossene, wasserdichte Schuhe.",
            "Le ciel s'assombrit, un orage est possible. Gardez un parapluie à portée de main et choisissez des chaussures fermées et imperméables.",
            "Il cielo è nuvoloso, è possibile un temporale. Tieni un ombrello a portata di mano e scegli scarpe chiuse e impermeabili.",
            "Osmon bulutli, momaqaldiroq bo'lishi mumkin. Soyabonni yaqin tuting va yopiq, suv o'tkazmaydigan poyabzal tanlang.",
            "Gökyüzü çatık, fırtına olabilir. Şemsiyenizi el altında bulundurun ve kapalı, su geçirmez ayakkabılar seçin.",
            "天空阴沉，可能有雷暴。请备好雨伞，并选择封闭的防水鞋。",
            "Le ciel est gris, un orage est possible. Garde un parapluie à portée de main et opte pour des chaussures fermées et imperméables.",
            "El cielo está nublado, es posible que haya una tormenta. Ten un paraguas a mano y elige zapatos cerrados e impermeables.",
            "ცა მოღრუბლულია, შესაძლებელია ჭექა-ქუხილი. იქონიეთ ქოლგა და აირჩიეთ დახურული, წყალგაუმტარი ფეხსაცმელი.",
            "Bầu trời u ám, có thể có dông. Hãy để ô ở gần và chọn giày kín, chống nước."
        ),
        "Грозовая погода требует осторожности: оденьтесь практично и спланируйте маршрут ближе к уютным местам." to arrayOf(
            "Найзағайлы ауа райы сақтықты талап етеді: практикалық киініп, жайлы жерлерге жақынырақ маршрут жоспарлаңыз.",
            "Stormy weather requires caution: dress practically and plan your route closer to cozy places.",
            "O tempo tempestuoso requer cautela: vista-se de forma prática e planeje sua rota mais perto de lugares aconchegantes.",
            "Stürmisches Wetter erfordert Vorsicht: Kleiden Sie sich praktisch und planen Sie Ihre Route näher an gemütlichen Orten.",
            "Le temps orageux demande de la prudence : habillez-vous de manière pratique et planifiez votre itinéraire plus près d'endroits confortables.",
            "Il tempo tempestoso richiede cautela: vestiti in modo pratico e pianifica il tuo percorso più vicino a luoghi accoglienti.",
            "Momaqaldiroqli ob-havo ehtiyotkorlikni talab qiladi: amaliy kiyining va qulay joylarga yaqinroq marshrut rejalashtiring.",
            "Fırtınalı hava dikkat gerektirir: pratik giyinin ve rotanızı rahat yerlere daha yakın planlayın.",
            "雷暴天气需要谨慎：穿着实用，并计划更靠近舒适地方的路线。",
            "Un temps orageux exige de la prudence : habille-toi de façon pratique et prévois un itinéraire près d'endroits douillets.",
            "El clima tormentoso requiere precaución: vístete de forma práctica y planifica tu ruta más cerca de lugares acogedores.",
            "შტორმიანი ამინდი მოითხოვს სიფრთხილეს: ჩაიცვით პრაქტიკულად და დაგეგმეთ თქვენი მარშრუტი მყუდრო ადგილებთან ახლოს.",
            "Thời tiết giông bão đòi hỏi sự thận trọng: ăn mặc thiết thực và lên kế hoạch lộ trình gần những nơi ấm cúng hơn."
        ),
        "За окном дождливо — обязательно возьмите с собой крепкий зонт и наденьте непромокаемую куртку." to arrayOf(
            "Терезе сыртында жаңбырлы — міндетті түрде берік қолшатыр алып, су өтпейтін куртка киіңіз.",
            "It's rainy outside — be sure to take a sturdy umbrella with you and wear a waterproof jacket.",
            "Está chuvoso lá fora — certifique-se de levar um guarda-chuva resistente com você e usar uma jaqueta impermeável.",
            "Draußen ist es regnerisch — nehmen Sie unbedingt einen stabilen Regenschirm mit und ziehen Sie eine wasserdichte Jacke an.",
            "Il pleut dehors - assurez-vous de prendre un parapluie solide avec vous et de porter une veste imperméable.",
            "Fuori piove — assicurati di portare con te un ombrello robusto e indossa una giacca impermeabile.",
            "Tashqarida yomg'ir yog'moqda — o'zingiz bilan mustahkam soyabon olishni va suv o'tkazmaydigan kurtka kiyishni unutmang.",
            "Dışarıda yağmur var — yanınıza sağlam bir şemsiye aldığınızdan ve su geçirmez bir ceket giydiğinizden emin olun.",
            "外面在下雨——一定要带上一把结实的雨伞并穿上防水夹克。",
            "Il pleut dehors — assure-toi de prendre un parapluie solide et de porter un manteau imperméable.",
            "Está lloviendo afuera — asegúrate de llevar un paraguas resistente y usar una chaqueta impermeable.",
            "გარეთ წვიმს — აუცილებლად წაიღეთ გამძლე ქოლგა და ჩაიცვით წყალგაუმტარი ქურთუკი.",
            "Ngoài trời đang mưa — hãy chắc chắn mang theo một chiếc ô chắc chắn và mặc áo khoác chống nước."
        ),
        "Сегодня город умывает дождь. Отличный повод надеть плащ с капюшоном и захватить тёплый напиток в дорогу." to arrayOf(
            "Бүгін қаланы жаңбыр жуып жатыр. Капюшоны бар плащ киіп, жолға жылы сусын алуға тамаша сылтау.",
            "Today the city is washed by rain. A great reason to wear a raincoat with a hood and grab a warm drink on the go.",
            "Hoje a cidade está lavada pela chuva. Um ótimo motivo para usar uma capa de chuva com capuz e tomar uma bebida quente para viagem.",
            "Heute wird die Stadt vom Regen reingewaschen. Ein toller Grund, einen Regenmantel mit Kapuze zu tragen und sich unterwegs ein warmes Getränk zu gönnen.",
            "Aujourd'hui, la ville est lavée par la pluie. Une excellente raison de porter un imperméable à capuche et de prendre une boisson chaude à emporter.",
            "Oggi la città è bagnata dalla pioggia. Un ottimo motivo per indossare un impermeabile con cappuccio e prendere una bevanda calda da asporto.",
            "Bugun shaharni yomg'ir yuvmoqda. Qopqoqli yomg'ir paltosini kiyish va yo'lda issiq ichimlik olish uchun ajoyib sabab.",
            "Bugün şehir yağmurla yıkanıyor. Kapüşonlu bir yağmurluk giymek ve hareket halindeyken sıcak bir içecek almak için harika bir neden.",
            "今天这座城市被雨水洗刷。这是穿上连帽雨衣并在路上买一杯热饮的好理由。",
            "La ville est lavée par la pluie aujourd'hui. Une belle occasion de mettre un imperméable à capuche et de prendre une boisson chaude pour la route.",
            "Hoy la ciudad se lava con la lluvia. Una gran excusa para usar un impermeable con capucha y llevar una bebida caliente para el camino.",
            "დღეს ქალაქს წვიმა რეცხავს. შესანიშნავი მიზეზი ჩაიცვათ საწვიმარი კაპიუშონით და თან წაიღოთ თბილი სასმელი გზაში.",
            "Hôm nay thành phố được tắm trong mưa. Một lý do tuyệt vời để mặc áo mưa có mũ trùm đầu và mang theo một thức uống ấm trên đường."
        ),
        "Ожидаются осадки: защитите обувь от влаги и не забудьте закинуть зонтик в сумку перед выходом." to arrayOf(
            "Жауын-шашын күтіледі: аяқ киімді ылғалдан қорғаңыз және шығар алдында қолшатырды сөмкеге салуды ұмытпаңыз.",
            "Precipitation is expected: protect your shoes from moisture and don't forget to throw an umbrella in your bag before going out.",
            "Prevê-se precipitação: proteja os seus sapatos da humidade e não se esqueça de deitar um guarda-chuva na mala antes de sair.",
            "Niederschlag wird erwartet: Schützen Sie Ihre Schuhe vor Feuchtigkeit und vergessen Sie nicht, vor dem Ausgehen einen Regenschirm in Ihre Tasche zu werfen.",
            "Des précipitations sont prévues : protégez vos chaussures de l'humidité et n'oubliez pas de glisser un parapluie dans votre sac avant de sortir.",
            "Sono previste precipitazioni: proteggi le tue scarpe dall'umidità e non dimenticare di buttare un ombrello in borsa prima di uscire.",
            "Yog'ingarchilik kutilmoqda: poyabzalingizni namlikdan himoya qiling va ko'chaga chiqishdan oldin sumkangizga soyabon solishni unutmang.",
            "Yağış bekleniyor: ayakkabılarınızı nemden koruyun ve çıkmadan önce çantanıza bir şemsiye atmayı unutmayın.",
            "预计有降水：保护好你的鞋子防潮，出门前别忘了把雨伞放进包里。",
            "Des précipitations sont attendues : protège tes chaussures de l'humidité et n'oublie pas de glisser un parapluie dans ton sac avant de sortir.",
            "Se esperan precipitaciones: protege tus zapatos de la humedad y no olvides echar un paraguas en tu bolso antes de salir.",
            "მოსალოდნელია ნალექი: დაიცავით ფეხსაცმელი ტენისგან და არ დაგავიწყდეთ ქოლგის ჩადება ჩანთაში გასვლამდე.",
            "Dự báo có mưa: hãy bảo vệ giày của bạn khỏi độ ẩm và đừng quên bỏ ô vào túi trước khi ra ngoài."
        ),
        "В воздухе висит лёгкая морось — пригодится ветровка с капюшоном или компактный складной зонт." to arrayOf(
            "Ауада жеңіл сіркіреген жаңбыр тұр — капюшоны бар желбегей немесе ықшам жиналмалы қолшатыр пайдалы болады.",
            "A light drizzle hangs in the air — a windbreaker with a hood or a compact folding umbrella will come in handy.",
            "Um chuvisco leve paira no ar — um quebra-vento com capuz ou um guarda-chuva dobrável compacto será útil.",
            "Ein leichter Nieselregen hängt in der Luft — eine Windjacke mit Kapuze oder ein kompakter Taschenschirm sind praktisch.",
            "Une légère bruine flotte dans l'air - un coupe-vent à capuche ou un parapluie pliant compact vous sera utile.",
            "C'è una leggera pioggerellina nell'aria: tornerà utile una giacca a vento con cappuccio o un ombrello pieghevole compatto.",
            "Havoda engil yomg'ir yog'moqda — qopqoqli shamoldan himoya qiluvchi kurtka yoki ixcham katlanadigan soyabon foydali bo'ladi.",
            "Havada hafif bir çiseleme var — kapüşonlu bir rüzgarlık veya kompakt katlanır bir şemsiye işe yarayacaktır.",
            "空气中弥漫着毛毛雨——带风帽的防风夹克或紧凑的折叠伞会派上用场。",
            "Une légère bruine flotte dans l'air — un coupe-vent à capuche ou un parapluie pliant compact sera utile.",
            "Hay una llovizna ligera en el aire: un cortavientos con capucha o un paraguas plegable compacto será útil.",
            "ჰაერში მსუბუქი ჟინჟღლია — ქარდამცავი კაპიუშონით ან კომპაქტური დასაკეცი ქოლგა გამოგადგებათ.",
            "Có mưa bụi nhẹ trong không khí — một chiếc áo gió có mũ hoặc một chiếc ô gấp nhỏ gọn sẽ rất hữu ích."
        ),
        "Моросящая погода создаёт особую атмосферу, но чтобы не промокнуть, лучше накинуть влагозащитную куртку." to arrayOf(
            "Сіркіреген ауа райы ерекше атмосфера жасайды, бірақ су болмас үшін ылғалдан қорғайтын куртка киген дұрыс.",
            "Drizzly weather creates a special atmosphere, but to avoid getting wet, it is better to throw on a waterproof jacket.",
            "O tempo chuvoso cria uma atmosfera especial, mas para não se molhar, é melhor vestir uma jaqueta impermeável.",
            "Nieselregen sorgt für eine besondere Atmosphäre, aber um nicht nass zu werden, ziehen Sie lieber eine wasserdichte Jacke an.",
            "Le temps bruineux crée une atmosphère particulière, mais pour éviter de se mouiller, il est préférable d'enfiler une veste imperméable.",
            "Il tempo piovigginoso crea un'atmosfera speciale, ma per non bagnarsi, è meglio indossare una giacca impermeabile.",
            "Yomg'irli ob-havo o'ziga xos muhit yaratadi, lekin ho'l bo'lmaslik uchun suv o'tkazmaydigan kurtka kiygan ma'qul.",
            "Çiseleyen hava özel bir atmosfer yaratır, ancak ıslanmamak için su geçirmez bir ceket giymek daha iyidir.",
            "毛毛雨天气营造出一种特殊的氛围，但为了避免淋湿，最好穿上防水夹克。",
            "Le temps bruineux crée une atmosphère spéciale, mais pour ne pas être mouillé, il vaut mieux enfiler un manteau imperméable.",
            "El clima lloviznoso crea un ambiente especial, pero para evitar mojarse, es mejor ponerse una chaqueta impermeable.",
            "წვიმიანი ამინდი ქმნის განსაკუთრებულ ატმოსფეროს, მაგრამ იმისათვის, რომ არ დასველდეთ, უკეთესია ჩაიცვათ წყალგაუმტარი ქურთუკი.",
            "Thời tiết mưa phùn tạo ra một bầu không khí đặc biệt, nhưng để không bị ướt, tốt hơn là nên mặc một chiếc áo khoác chống nước."
        ),
        "Лёгкие осадки и повышенная влажность: советуем одеться многослойно и захватить зонт на всякий случай." to arrayOf(
            "Жеңіл жауын-шашын және жоғары ылғалдылық: қабаттап киінуге және кез келген жағдайда қолшатыр алуға кеңес береміз.",
            "Light precipitation and high humidity: we advise you to dress in layers and take an umbrella just in case.",
            "Precipitação leve e alta umidade: aconselhamos vestir-se em camadas e levar um guarda-chuva, por via das dúvidas.",
            "Leichter Niederschlag und hohe Luftfeuchtigkeit: Wir raten Ihnen, sich im Zwiebellook zu kleiden und für alle Fälle einen Regenschirm mitzunehmen.",
            "Précipitations légères et humidité élevée : nous vous conseillons de vous habiller en plusieurs couches et de prendre un parapluie au cas où.",
            "Precipitazioni leggere e umidità elevata: ti consigliamo di vestirti a strati e prendere un ombrello per ogni evenienza.",
            "Yengil yog'ingarchilik va yuqori namlik: qatlamli kiyinishni va har ehtimolga qarshi soyabon olishingizni maslahat beramiz.",
            "Hafif yağış ve yüksek nem: kat kat giyinmenizi ve her ihtimale karşı bir şemsiye almanızı tavsiye ederiz.",
            "小雨和高湿度：我们建议您分层穿着，并带上一把伞以防万一。",
            "Précipitations légères et forte humidité : nous te conseillons de t'habiller en multicouches et de prendre un parapluie au cas où.",
            "Precipitaciones ligeras y alta humedad: te aconsejamos vestirte en capas y llevar un paraguas por si acaso.",
            "მსუბუქი ნალექი და მაღალი ტენიანობა: გირჩევთ ჩაიცვათ ფენებად და წაიღოთ ქოლგა ყოველი შემთხვევისთვის.",
            "Lượng mưa nhẹ và độ ẩm cao: chúng tôi khuyên bạn nên mặc nhiều lớp và mang theo ô phòng hờ."
        ),
        "Кружатся снежинки и подмораживает — самое время для тёплого пуховика, шарфа и перчаток." to arrayOf(
            "Қар ұшқындап, аяз болып тұр — жылы пуховик, шарф және қолғап киетін уақыт.",
            "Snowflakes are swirling and it is freezing — it's high time for a warm down jacket, scarf and gloves.",
            "Flocos de neve giram e está congelando — é a hora certa para uma jaqueta quente, cachecol e luvas.",
            "Schneeflocken wirbeln und es friert — höchste Zeit für eine warme Daunenjacke, Schal und Handschuhe.",
            "Les flocons de neige tourbillonnent et il gèle - il est grand temps de mettre une doudoune chaude, une écharpe et des gants.",
            "I fiocchi di neve turbinano e si gela: è il momento giusto per un piumino caldo, una sciarpa e dei guanti.",
            "Qor parchalari aylanmoqda va sovuq tushmoqda — issiq kurtka, sharf va qo'lqop kiyish vaqti keldi.",
            "Kar taneleri dönüyor ve donuyor — sıcak tutan bir şişme mont, atkı ve eldiven için tam zamanı.",
            "雪花飞舞，正在结冰——正是穿上保暖羽绒服、围巾和手套的时候。",
            "Les flocons de neige tourbillonnent et il gèle — c'est le moment idéal pour une doudoune chaude, une écharpe et des gants.",
            "Los copos de nieve se arremolinan y está helando: es el momento adecuado para una chaqueta abrigada, bufanda y guantes.",
            "ფიფქები ტრიალებს და ყინავს — ზუსტად დროა თბილი ქურთუკისთვის, შარფისთვის და ხელთათმანებისთვის.",
            "Bông tuyết đang bay và trời đang đóng băng - đã đến lúc mặc áo khoác phao ấm, quàng khăn và đeo găng tay."
        ),
        "Сегодня снежно: выбирайте зимнюю обувь с нескользящей подошвой и одевайтесь максимально тепло." to arrayOf(
            "Бүгін қар жауып тұр: сырғанамайтын табаны бар қысқы аяқ киімді таңдаңыз және мейлінше жылы киініңіз.",
            "It is snowy today: choose winter shoes with non-slip soles and dress as warmly as possible.",
            "Está nevando hoje: escolha sapatos de inverno com sola antiderrapante e vista-se o mais quente possível.",
            "Heute ist es schneereich: Wählen Sie Winterschuhe mit rutschfester Sohle und kleiden Sie sich so warm wie möglich.",
            "Il neige aujourd'hui : choisissez des chaussures d'hiver avec des semelles antidérapantes et habillez-vous le plus chaudement possible.",
            "Oggi nevica: scegli scarpe invernali con suola antiscivolo e vestiti il più caldo possibile.",
            "Bugun qorli: sirpanmaydigan taglikli qishki poyabzal tanlang va iloji boricha issiq kiyining.",
            "Bugün karlı: kaymaz tabanlı kışlık ayakkabılar seçin ve olabildiğince sıcak giyinin.",
            "今天下雪：选择防滑底的冬鞋，并穿得尽可能暖和。",
            "Il neige aujourd'hui : choisis des chaussures d'hiver à semelles antidérapantes et habille-toi aussi chaudement que possible.",
            "Hoy está nevando: elige zapatos de invierno con suela antideslizante y vístete lo más abrigado posible.",
            "დღეს თოვს: აირჩიეთ ზამთრის ფეხსაცმელი მოცურების საწინააღმდეგო ძირით და ჩაიცვით რაც შეიძლება თბილად.",
            "Hôm nay trời có tuyết: hãy chọn giày mùa đông có đế chống trượt và mặc càng ấm càng tốt."
        ),
        "Настоящая зимняя погода со снегом — укутайтесь потеплее, не забудьте шапку и берегите руки от холода." to arrayOf(
            "Қар жауған нағыз қысқы ауа райы — жылырақ ораныңыз, бас киімді ұмытпаңыз және қолдарыңызды суықтан қорғаңыз.",
            "Real winter weather with snow — wrap up warmer, don't forget a hat and protect your hands from the cold.",
            "Clima de inverno real com neve — agasalhe-se bem, não se esqueça do chapéu e proteja as mãos do frio.",
            "Echtes Winterwetter mit Schnee — packen Sie sich wärmer ein, vergessen Sie nicht eine Mütze und schützen Sie Ihre Hände vor der Kälte.",
            "Un vrai temps d'hiver avec de la neige - couvrez-vous chaudement, n'oubliez pas un chapeau et protégez vos mains du froid.",
            "Vero clima invernale con neve: copriti bene, non dimenticare un cappello e proteggi le mani dal freddo.",
            "Qorli haqiqiy qishki ob-havo — issiqroq o'raning, shlyapani unutmang va qo'llaringizni sovuqdan saqlang.",
            "Karlı gerçek kış havası — daha sıkı giyinin, bir şapka takmayı unutmayın ve ellerinizi soğuktan koruyun.",
            "真正的下雪冬日天气——穿暖和点，别忘了戴帽子，保护双手免受寒冷。",
            "Un vrai temps d'hiver avec de la neige — couvre-toi chaudement, n'oublie pas une tuque et protège tes mains du froid.",
            "Verdadero clima de invierno con nieve: abrígate más, no olvides un sombrero y protege tus manos del frío.",
            "ნამდვილი ზამთრის ამინდი თოვლით — ჩაიცვით უფრო თბილად, არ დაგავიწყდეთ ქუდი და დაიცავით ხელები სიცივისგან.",
            "Thời tiết mùa đông thực sự có tuyết - hãy quấn ấm hơn, đừng quên mũ và bảo vệ đôi tay của bạn khỏi giá lạnh."
        ),
        "На улице морозно и свежо: обязательно наденьте зимнюю куртку, тёплую шапку и перчатки." to arrayOf(
            "Далада аяз және таза ауа: міндетті түрде қысқы куртка, жылы бас киім және қолғап киіңіз.",
            "It's frosty and fresh outside: be sure to wear a winter jacket, a warm hat and gloves.",
            "Está gelado e fresco lá fora: certifique-se de usar uma jaqueta de inverno, um chapéu quente e luvas.",
            "Draußen ist es frostig und frisch: Ziehen Sie unbedingt eine Winterjacke, eine warme Mütze und Handschuhe an.",
            "Il fait glacial et frais dehors : assurez-vous de porter une veste d'hiver, un bonnet chaud et des gants.",
            "Fuori è gelido e fresco: assicurati di indossare una giacca invernale, un cappello caldo e dei guanti.",
            "Tashqarida sovuq va musaffo: albatta qishki kurtka, issiq shlyapa va qo'lqop kiying.",
            "Dışarıda ayaz ve temiz bir hava var: mutlaka kışlık bir ceket, sıcak tutan bir şapka ve eldiven giyin.",
            "外面寒冷刺骨，空气清新：一定要穿上冬装外套，戴上保暖的帽子和手套。",
            "Il fait glacial et frais dehors : assure-toi de porter un manteau d'hiver, une tuque chaude et des gants.",
            "Hace mucho frío y fresco afuera: asegúrate de usar una chaqueta de invierno, un sombrero abrigado y guantes.",
            "გარეთ ყინვიანი და გრილი ამინდია: აუცილებლად ჩაიცვით ზამთრის ქურთუკი, თბილი ქუდი და ხელთათმანები.",
            "Ngoài trời rất lạnh và trong lành: hãy chắc chắn mặc áo khoác mùa đông, đội mũ ấm và đeo găng tay."
        ),
        "Температура опустилась ниже нуля — выбирайте термобельё или тёплый свитер, чтобы чувствовать себя комфортно." to arrayOf(
            "Температура нөлден төмен түсті — өзіңізді жайлы сезіну үшін термоішкі киім немесе жылы свитер таңдаңыз.",
            "The temperature has dropped below zero — choose thermal underwear or a warm sweater to feel comfortable.",
            "A temperatura caiu abaixo de zero — escolha roupas íntimas térmicas ou um suéter quente para se sentir confortável.",
            "Die Temperatur ist unter null gefallen — wählen Sie Thermounterwäsche oder einen warmen Pullover, um sich wohl zu fühlen.",
            "La température est descendue en dessous de zéro - choisissez des sous-vêtements thermiques ou un pull chaud pour vous sentir à l'aise.",
            "La temperatura è scesa sotto lo zero: scegli biancheria intima termica o un maglione caldo per stare comodo.",
            "Harorat noldan pastga tushdi — o'zingizni qulay his qilish uchun termal ichki kiyim yoki issiq sviter tanlang.",
            "Sıcaklık sıfırın altına düştü — rahat hissetmek için termal iç çamaşırı veya sıcak tutan bir kazak seçin.",
            "气温已降至零下——选择保暖内衣或保暖毛衣以感觉舒适。",
            "La température a chuté sous zéro — choisis des sous-vêtements thermiques ou un pull chaud pour te sentir confortable.",
            "La temperatura ha bajado de cero: elige ropa interior térmica o un suéter abrigado para sentirte cómodo.",
            "ტემპერატურა ნულს ქვემოთ დაეცა — აირჩიეთ თერმული საცვლები ან თბილი სვიტერი, რომ თავი კომფორტულად იგრძნოთ.",
            "Nhiệt độ đã giảm xuống dưới 0 — hãy chọn đồ lót giữ nhiệt hoặc áo len ấm áp để cảm thấy thoải mái."
        ),
        "Холодный воздух бодрит, но требует утепления: плотная верхняя одежда и шарф сегодня незаменимы." to arrayOf(
            "Салқын ауа сергітеді, бірақ жылынуды талап етеді: қалың сырт киім мен шарф бүгін өте қажет.",
            "The cold air is invigorating, but requires insulation: dense outerwear and a scarf are indispensable today.",
            "O ar frio é revigorante, mas requer isolamento: agasalhos grossos e um cachecol são indispensáveis hoje.",
            "Die kalte Luft ist belebend, erfordert aber Isolierung: Dichte Oberbekleidung und ein Schal sind heute unverzichtbar.",
            "L'air froid est revigorant, mais nécessite de l'isolation : des vêtements d'extérieur épais et une écharpe sont indispensables aujourd'hui.",
            "L'aria fredda è corroborante, ma richiede isolamento: capispalla pesanti e una sciarpa sono indispensabili oggi.",
            "Sovuq havo tetiklashtiradi, lekin isinishni talab qiladi: qalin ustki kiyim va sharf bugun almashtirib bo'lmaydigan narsalardir.",
            "Soğuk hava canlandırıcıdır, ancak yalıtım gerektirir: kalın dış giyim ve bir atkı bugün vazgeçilmezdir.",
            "冷空气令人振奋，但需要保暖：今天厚实的外套和围巾是必不可少的。",
            "L'air froid est vivifiant, mais demande de bien se couvrir : des vêtements d'extérieur épais et une écharpe sont indispensables aujourd'hui.",
            "El aire frío es estimulante, pero requiere aislamiento: ropa de abrigo gruesa y una bufanda son indispensables hoy.",
            "ცივი ჰაერი გამამხნევებელია, მაგრამ მოითხოვს დათბობას: სქელი ზედა ტანსაცმელი და შარფი დღეს შეუცვლელია.",
            "Không khí lạnh rất sảng khoái nhưng cần giữ ấm: áo khoác ngoài dày và khăn quàng cổ là không thể thiếu trong ngày hôm nay."
        ),
        "Сегодня довольно холодно и зябко — советуем надеть утеплённую куртку или пальто со свитером." to arrayOf(
            "Бүгін айтарлықтай суық әрі ызғарлы — жылытылған куртка немесе свитермен пальто киюге кеңес береміз.",
            "Today it is quite cold and chilly — we advise you to wear an insulated jacket or coat with a sweater.",
            "Hoje está bastante frio e gelado — aconselhamos usar uma jaqueta isolada ou um casaco com um suéter.",
            "Heute ist es ziemlich kalt und frostig — wir raten Ihnen, eine isolierte Jacke oder einen Mantel mit einem Pullover zu tragen.",
            "Aujourd'hui, il fait assez froid et frisquet - nous vous conseillons de porter une veste isolante ou un manteau avec un pull.",
            "Oggi fa piuttosto freddo e c'è un clima pungente: ti consigliamo di indossare una giacca imbottita o un cappotto con un maglione.",
            "Bugun ancha sovuq va izg'irin — issiq kurtka yoki sviter bilan palto kiyishingizni maslahat beramiz.",
            "Bugün oldukça soğuk ve dondurucu — yalıtımlı bir ceket veya kazakla bir palto giymenizi tavsiye ederiz.",
            "今天相当冷且刺骨——我们建议你穿上保暖夹克或搭配毛衣的外套。",
            "Il fait assez froid et frisquet aujourd'hui — nous te conseillons de porter un manteau isolé ou une veste avec un pull.",
            "Hoy hace bastante frío y es helado: te aconsejamos que uses una chaqueta aislada o un abrigo con un suéter.",
            "დღეს საკმაოდ ცივა და სუსხია — გირჩევთ ჩაიცვათ დათბილული ქურთუკი ან პალტო სვიტერთან ერთად.",
            "Hôm nay khá lạnh và ớn lạnh — chúng tôi khuyên bạn nên mặc áo khoác cách nhiệt hoặc áo dạ cùng với áo len."
        ),
        "Прохладный день: чтобы ветер не застал врасплох, выбирайте закрытую одежду и захватите шарф." to arrayOf(
            "Салқын күн: жел күтпеген жерден ұстамауы үшін жабық киім таңдап, шарф алыңыз.",
            "A cool day: so that the wind does not take you by surprise, choose closed clothes and grab a scarf.",
            "Um dia fresco: para que o vento não o pegue de surpresa, escolha roupas fechadas e pegue um cachecol.",
            "Ein kühler Tag: Damit der Wind Sie nicht überrascht, wählen Sie geschlossene Kleidung und nehmen Sie einen Schal mit.",
            "Une journée fraîche : pour que le vent ne vous surprenne pas, choisissez des vêtements fermés et prenez une écharpe.",
            "Una giornata fresca: affinché il vento non ti colga di sorpresa, scegli abiti chiusi e prendi una sciarpa.",
            "Salqin kun: shamol sizni ajablantirmasligi uchun yopiq kiyim tanlang va sharf oling.",
            "Serin bir gün: rüzgarın sizi şaşırtmaması için kapalı giysiler seçin ve bir atkı alın.",
            "凉爽的一天：为了不让风使你措手不及，请选择封闭的衣服并带上围巾。",
            "Une journée fraîche : pour que le vent ne te surprenne pas, choisis des vêtements couvrants et prends une écharpe.",
            "Un día fresco: para que el viento no te tome por sorpresa, elige ropa cerrada y toma una bufanda.",
            "გრილი დღე: იმისათვის, რომ ქარმა არ გაგაკვირვოთ, აირჩიეთ დახურული ტანსაცმელი და წაიღეთ შარფი.",
            "Một ngày mát mẻ: để gió không làm bạn bất ngờ, hãy chọn quần áo kín và mang theo một chiếc khăn quàng cổ."
        ),
        "За окном бодрящая прохлада. Многослойный образ с тёплой кофтой и курткой подойдёт идеально." to arrayOf(
            "Терезе сыртында сергітетін салқындық. Жылы кофта және курткамен көп қабатты образ тамаша үйлеседі.",
            "There is an invigorating coolness outside. A layered look with a warm cardigan and jacket will fit perfectly.",
            "Há um frescor revigorante lá fora. Um visual em camadas com um cardigã quente e uma jaqueta cairá perfeitamente.",
            "Draußen herrscht eine belebende Kühle. Ein mehrlagiger Look mit einer warmen Strickjacke und einer Jacke passt perfekt.",
            "Il y a une fraîcheur vivifiante dehors. Un look superposé avec un gilet chaud et une veste conviendra parfaitement.",
            "C'è una freschezza corroborante fuori. Un look a strati con un caldo cardigan e una giacca si adatterà perfettamente.",
            "Tashqarida tetiklantiruvchi salqinlik bor. Issiq kofta va kurtka bilan qatlamli kiyinish mukammal mos keladi.",
            "Dışarıda canlandırıcı bir serinlik var. Sıcak bir hırka ve ceketle kat kat bir görünüm mükemmel uyum sağlayacaktır.",
            "外面有令人振奋的凉爽。搭配保暖开衫和夹克的分层穿搭将非常适合。",
            "Une fraîcheur vivifiante t'attend dehors. Un look superposé avec un cardigan chaud et une veste sera parfait.",
            "Hay una frescura estimulante afuera. Un look en capas con un cárdigan cálido y una chaqueta quedará perfecto.",
            "გარეთ გამამხნევებელი სიგრილეა. მრავალფენიანი ლუქი თბილი ზედატანით და ქურთუკით იდეალური იქნება.",
            "Ngoài trời có sự mát mẻ sảng khoái. Một phong cách xếp lớp với áo len ấm áp và áo khoác sẽ hoàn toàn phù hợp."
        ),
        "Умеренная прохлада: отлично подойдёт демисезонная куртка, тренч или плотная толстовка." to arrayOf(
            "Қалыпты салқындық: демисезондық куртка, тренч немесе қалың толстовка өте жақсы үйлеседі.",
            "Moderate coolness: a demi-season jacket, trench coat or thick hoodie will suit perfectly.",
            "Frescor moderado: uma jaqueta meia-estação, sobretudo ou moletom grosso cairão perfeitamente.",
            "Mäßige Kühle: Eine Übergangsjacke, ein Trenchcoat oder ein dicker Hoodie passen perfekt.",
            "Fraîcheur modérée : une veste de mi-saison, un trench-coat ou un sweat à capuche épais conviendront parfaitement.",
            "Fresco moderato: una giacca per le mezze stagioni, un trench o una felpa con cappuccio spessa si adatteranno perfettamente.",
            "O'rtacha salqinlik: mavsumiy kurtka, trench yoki qalin tolstovka juda mos keladi.",
            "Ilıman serinlik: mevsimlik bir ceket, trençkot veya kalın bir kapüşonlu mükemmel uyum sağlayacaktır.",
            "适度凉爽：季中夹克、风衣或厚连帽衫将完美契合。",
            "Fraîcheur modérée : une veste de mi-saison, un trench ou un sweat à capuche épais seront parfaits.",
            "Frescura moderada: una chaqueta de entretiempo, una gabardina o una sudadera con capucha gruesa te quedarán perfectas.",
            "ზომიერი სიგრილე: დემისეზონური ქურთუკი, ტრენჩი ან სქელი ჰუდი იდეალური იქნება.",
            "Mát mẻ vừa phải: một chiếc áo khoác giao mùa, áo khoác dáng dài hoặc áo hoodie dày sẽ rất phù hợp."
        ),
        "На улице свежо, но вполне комфортно — накиньте ветровку или лёгкое пальто для приятной прогулки." to arrayOf(
            "Далада салқын, бірақ өте жайлы — жағымды серуендеу үшін желбегей немесе жеңіл пальто киіп алыңыз.",
            "It is fresh outside, but quite comfortable — throw on a windbreaker or a light coat for a pleasant walk.",
            "Está fresco lá fora, mas bastante confortável — vista um quebra-vento ou um casaco leve para um passeio agradável.",
            "Draußen ist es frisch, aber recht angenehm — werfen Sie eine Windjacke oder einen leichten Mantel für einen angenehmen Spaziergang über.",
            "Il fait frais dehors, mais assez confortable - enfilez un coupe-vent ou un manteau léger pour une promenade agréable.",
            "Fuori è fresco, ma abbastanza confortevole: indossa una giacca a vento o un cappotto leggero per una piacevole passeggiata.",
            "Tashqarida toza, lekin juda qulay — yoqimli sayr qilish uchun shamoldan himoya qiluvchi kurtka yoki yengil palto kiyib oling.",
            "Dışarıda temiz ama oldukça rahat bir hava var — keyifli bir yürüyüş için bir rüzgarlık veya hafif bir palto giyin.",
            "外面空气清新，但很舒服——穿上风衣或轻便外套，享受愉快的散步。",
            "Il fait frais dehors, mais assez confortable — mets un coupe-vent ou un manteau léger pour une belle promenade.",
            "Hace fresco afuera, pero bastante cómodo: ponte un cortavientos o un abrigo ligero para un paseo agradable.",
            "გარეთ გრილა, მაგრამ საკმაოდ კომფორტულია — მოიცვით ქარდამცავი ან მსუბუქი პალტო სასიამოვნო სეირნობისთვის.",
            "Ngoài trời rất trong lành, nhưng khá thoải mái — hãy khoác một chiếc áo gió hoặc áo dạ nhẹ để có một buổi dạo chơi thú vị."
        ),
        "Погода переменчива: днём может быть уютно в худи, но к вечеру точно пригодится куртка." to arrayOf(
            "Ауа райы құбылмалы: күндіз худиде жайлы болуы мүмкін, бірақ кешке міндетті түрде куртка қажет болады.",
            "The weather is changeable: it may be cozy in a hoodie during the day, but a jacket will definitely come in handy by the evening.",
            "O tempo é instável: pode ser aconchegante com um moletom com capuz durante o dia, mas uma jaqueta definitivamente será útil à noite.",
            "Das Wetter ist wechselhaft: Tagsüber ist es in einem Hoodie vielleicht gemütlich, aber gegen Abend wird auf jeden Fall eine Jacke praktisch sein.",
            "La météo est changeante : il peut être confortable de porter un sweat à capuche pendant la journée, mais une veste sera certainement utile d'ici le soir.",
            "Il tempo è variabile: potrebbe fare comodo una felpa con cappuccio durante il giorno, ma una giacca tornerà sicuramente utile entro sera.",
            "Ob-havo o'zgaruvchan: kunduzi xudida qulay bo'lishi mumkin, lekin kechqurun kurtka albatta kerak bo'ladi.",
            "Hava değişken: gündüzleri kapüşonlu bir tişörtle rahat olabilir, ancak akşam saatlerinde bir ceket kesinlikle işe yarayacaktır.",
            "天气多变：白天穿连帽衫可能会很舒服，但到了晚上肯定会用得上一件夹克。",
            "La météo est changeante : un sweat à capuche peut suffire de jour, mais une veste sera très utile en soirée.",
            "El clima es cambiante: puede que estés cómodo con una sudadera con capucha durante el día, pero una chaqueta te vendrá muy bien al anochecer.",
            "ამინდი ცვალებადია: დღისით ჰუდით შეიძლება კომფორტულად იყოთ, მაგრამ საღამოსკენ ქურთუკი აუცილებლად დაგჭირდებათ.",
            "Thời tiết hay thay đổi: ban ngày mặc áo hoodie có thể thoải mái, nhưng đến tối một chiếc áo khoác chắc chắn sẽ rất hữu ích."
        ),
        "Мягкая и очень комфортная погода! Достаточно лёгкой кофты, рубашки или джинсовки для долгой прогулки." to arrayOf(
            "Жұмсақ және өте жайлы ауа райы! Ұзақ серуендеу үшін жеңіл кофта, жейде немесе джинсы куртка жеткілікті.",
            "Mild and very comfortable weather! A light cardigan, shirt or denim jacket is enough for a long walk.",
            "Tempo ameno e muito confortável! Um cardigã leve, camisa ou jaqueta jeans é o suficiente para uma longa caminhada.",
            "Mildes und sehr angenehmes Wetter! Eine leichte Strickjacke, ein Hemd oder eine Jeansjacke reichen für einen langen Spaziergang.",
            "Un temps doux et très agréable ! Un gilet léger, une chemise ou une veste en jean suffisent pour une longue promenade.",
            "Tempo mite e molto confortevole! Un cardigan leggero, una camicia o una giacca di jeans sono sufficienti per una lunga passeggiata.",
            "Yumshoq va juda qulay ob-havo! Uzoq sayr qilish uchun yengil kofta, ko'ylak yoki jinsi kurtka yetarli.",
            "Ilıman ve çok rahat bir hava! Uzun bir yürüyüş için hafif bir hırka, gömlek veya kot ceket yeterlidir.",
            "温和非常舒适的天气！一件轻薄的开衫、衬衫或牛仔夹克就足以进行长途散步。",
            "Temps doux et très confortable ! Une veste légère, une chemise ou une veste en jean suffiront pour une longue marche.",
            "¡Clima templado y muy agradable! Un cárdigan ligero, camisa o chaqueta vaquera es suficiente para una caminata larga.",
            "რბილი და ძალიან კომფორტული ამინდია! მსუბუქი ზედა, პერანგი ან ჯინსის ქურთუკი საკმარისია ხანგრძლივი სეირნობისთვის.",
            "Thời tiết ôn hòa và rất thoải mái! Một chiếc áo len nhẹ, áo sơ mi hoặc áo khoác denim là đủ cho một chuyến đi bộ dài."
        ),
        "Идеальный баланс тепла и свежести — одевайтесь свободно, но захватите лёгкую накидку на вечер." to arrayOf(
            "Жылу мен тазалықтың мінсіз теңгерімі — еркін киініңіз, бірақ кешке жеңіл жамылғы ала шығыңыз.",
            "Perfect balance of warmth and freshness — dress loosely, but grab a light cape for the evening.",
            "Equilíbrio perfeito entre calor e frescor — vista-se de forma folgada, mas pegue uma capa leve para a noite.",
            "Perfekte Balance aus Wärme und Frische — kleiden Sie sich locker, aber nehmen Sie für den Abend ein leichtes Cape mit.",
            "Équilibre parfait entre chaleur et fraîcheur - habillez-vous amplement, mais prenez une cape légère pour la soirée.",
            "Perfetto equilibrio di calore e freschezza: vestiti comodamente, ma prendi una mantellina leggera per la sera.",
            "Issiqlik va tozalikning mukammal muvozanati — erkin kiyining, lekin kechqurun uchun yengil plash oling.",
            "Sıcaklık ve ferahlığın mükemmel dengesi — rahat giyinin, ancak akşam için hafif bir pelerin alın.",
            "温暖与清新的完美平衡——穿着宽松，但带上一件轻薄的披风以备晚上使用。",
            "Un équilibre parfait entre chaleur et fraîcheur — habille-toi de façon détendue, mais prévois une petite laine pour la soirée.",
            "Equilibrio perfecto entre calidez y frescura: vístete holgado, pero lleva una capa ligera para la noche.",
            "სითბოსა და სიგრილის იდეალური ბალანსი — ჩაიცვით თავისუფლად, მაგრამ თან წაიღეთ მსუბუქი მოსაცმელი საღამოსთვის.",
            "Sự cân bằng hoàn hảo giữa sự ấm áp và trong lành — ăn mặc thoải mái, nhưng mang theo một chiếc áo choàng nhẹ cho buổi tối."
        ),
        "Замечательная погода за окном: самое время пройтись пешком на свежем воздухе в лёгкой и удобной одежде." to arrayOf(
            "Терезе сыртында керемет ауа райы: жеңіл әрі ыңғайлы киіммен таза ауада жаяу жүретін уақыт.",
            "Wonderful weather outside: it's high time to take a walk in the fresh air in light and comfortable clothes.",
            "Tempo maravilhoso lá fora: é a hora certa de dar um passeio ao ar livre com roupas leves e confortáveis.",
            "Wunderbares Wetter draußen: Es ist höchste Zeit für einen Spaziergang an der frischen Luft in leichter und bequemer Kleidung.",
            "Temps magnifique dehors : il est grand temps de se promener au grand air dans des vêtements légers et confortables.",
            "Tempo meraviglioso fuori: è il momento giusto per fare una passeggiata all'aria aperta con abiti leggeri e comodi.",
            "Tashqarida ajoyib ob-havo: yengil va qulay kiyimda toza havoda sayr qilishning ayni vaqti.",
            "Dışarıda harika bir hava: hafif ve rahat kıyafetlerle temiz havada yürüyüş yapmanın tam zamanı.",
            "外面的天气棒极了：正是穿着轻便舒适的衣服在新鲜空气中散步的好时机。",
            "Il fait un temps magnifique dehors : c'est le moment idéal pour une marche au grand air avec des vêtements légers et confortables.",
            "Un clima maravilloso afuera: es hora de dar un paseo al aire libre con ropa ligera y cómoda.",
            "გარეთ შესანიშნავი ამინდია: ზუსტად დროა სუფთა ჰაერზე გაისეირნოთ მსუბუქი და კომფორტული ტანსაცმლით.",
            "Thời tiết bên ngoài tuyệt vời: đã đến lúc đi dạo hóng gió mát trong những bộ quần áo nhẹ nhàng và thoải mái."
        ),
        "На улице по-летнему тепло и приятно! Выбирайте лёгкую дышащую одежду и наслаждайтесь солнечным днём." to arrayOf(
            "Далада жаздағыдай жылы әрі жағымды! Жеңіл, дем алатын киімді таңдап, шуақты күннің рахатын көріңіз.",
            "It is summery warm and pleasant outside! Choose light, breathable clothes and enjoy a sunny day.",
            "Está um calor de verão e muito agradável lá fora! Escolha roupas leves e respiráveis e desfrute de um dia de sol.",
            "Draußen ist es sommerlich warm und angenehm! Wählen Sie leichte, atmungsaktive Kleidung und genießen Sie einen sonnigen Tag.",
            "Il fait chaud et agréable, comme en été ! Choisissez des vêtements légers et respirants et profitez d'une journée ensoleillée.",
            "Fuori fa un caldo estivo ed è piacevole! Scegli abiti leggeri e traspiranti e goditi una giornata di sole.",
            "Tashqarida yozgidek issiq va yoqimli! Yengil, nafas oladigan kiyim tanlang va quyoshli kundan zavqlaning.",
            "Dışarıda yaz gibi sıcak ve hoş bir hava var! Hafif, nefes alabilen kıyafetler seçin ve güneşli bir günün tadını çıkarın.",
            "外面充满夏日的温暖和惬意！选择轻便透气的衣服，享受阳光明媚的一天。",
            "C'est l'été dehors, chaud et agréable ! Choisis des vêtements légers et respirants, et profite de la journée ensoleillée.",
            "¡Hace un calor veraniego y es agradable afuera! Elige ropa ligera y transpirable y disfruta de un día soleado.",
            "გარეთ ზაფხულივით თბილი და სასიამოვნო ამინდია! აირჩიეთ მსუბუქი ჰაეროვანი ტანსაცმელი და დატკბით მზიანი დღით.",
            "Ngoài trời ấm áp và dễ chịu như mùa hè! Hãy chọn quần áo nhẹ nhàng, thoáng khí và tận hưởng một ngày nắng đẹp."
        ),
        "Тёплый и ласковый день — футболка и лёгкие брюки или шорты обеспечат максимальный комфорт." to arrayOf(
            "Жылы әрі жұмсақ күн — футболка мен жеңіл шалбар немесе шорт барынша жайлылықты қамтамасыз етеді.",
            "A warm and affectionate day — a T-shirt and light trousers or shorts will provide maximum comfort.",
            "Um dia quente e afetuoso — uma camiseta e calças leves ou shorts proporcionarão o máximo conforto.",
            "Ein warmer und liebevoller Tag — ein T-Shirt und eine leichte Hose oder Shorts sorgen für maximalen Komfort.",
            "Une journée chaude et affectueuse - un t-shirt et un pantalon léger ou un short offriront un confort maximal.",
            "Una giornata calda e affettuosa: una maglietta e pantaloni leggeri o pantaloncini garantiranno il massimo comfort.",
            "Issiq va muloyim kun — futbolka va yengil shim yoki shorti maksimal qulaylikni ta'minlaydi.",
            "Sıcak ve sevecen bir gün — bir tişört ve hafif pantolon veya şort maksimum rahatlık sağlayacaktır.",
            "温暖而温柔的一天——一件T恤和轻便的裤子或短裤将提供最大的舒适度。",
            "Une journée douce et chaleureuse — un t-shirt avec des pantalons légers ou des shorts t'offriront un maximum de confort.",
            "Un día cálido y cariñoso: una camiseta y pantalones ligeros o pantalones cortos proporcionarán el máximo confort.",
            "თბილი და ნაზი დღეა — მაისური და მსუბუქი შარვალი ან შორტები უზრუნველყოფს მაქსიმალურ კომფორტს.",
            "Một ngày ấm áp và dễ thương - một chiếc áo thun và quần dài nhẹ hoặc quần đùi sẽ mang lại sự thoải mái tối đa."
        ),
        "Отличная тёплая погода: тяжёлые вещи можно оставить дома, но не забудьте солнцезащитные очки." to arrayOf(
            "Керемет жылы ауа райы: ауыр заттарды үйде қалдыруға болады, бірақ күннен қорғайтын көзілдірікті ұмытпаңыз.",
            "Excellent warm weather: heavy clothes can be left at home, but do not forget your sunglasses.",
            "Excelente tempo quente: roupas pesadas podem ser deixadas em casa, mas não se esqueça dos óculos de sol.",
            "Hervorragendes warmes Wetter: Schwere Kleidung kann zu Hause bleiben, aber vergessen Sie nicht Ihre Sonnenbrille.",
            "Excellent temps chaud : les vêtements lourds peuvent être laissés à la maison, mais n'oubliez pas vos lunettes de soleil.",
            "Clima caldo eccellente: i vestiti pesanti possono essere lasciati a casa, ma non dimenticare gli occhiali da sole.",
            "Ajoyib issiq ob-havo: og'ir kiyimlarni uyda qoldirish mumkin, lekin quyoshdan saqlaydigan ko'zoynaklarni unutmang.",
            "Mükemmel sıcak hava: kalın giysiler evde bırakılabilir, ancak güneş gözlüğünüzü unutmayın.",
            "极好的温暖天气：厚重的衣服可以留在家里，但别忘了戴墨镜。",
            "Un temps chaud parfait : tu peux laisser les vêtements lourds à la maison, mais n'oublie pas tes lunettes de soleil.",
            "Excelente clima cálido: la ropa pesada puede quedarse en casa, pero no olvides tus gafas de sol.",
            "მშვენიერი თბილი ამინდია: მძიმე ტანსაცმელი შეგიძლიათ სახლში დატოვოთ, მაგრამ არ დაგავიწყდეთ მზის სათვალე.",
            "Thời tiết ấm áp tuyệt vời: có thể để lại áo quần nặng ở nhà, nhưng đừng quên kính râm của bạn."
        ),
        "Сегодня по-настоящему жарко! Одевайтесь максимально легко, выбирайте светлые ткани и пейте больше воды." to arrayOf(
            "Бүгін шынымен ыстық! Барынша жеңіл киініңіз, ашық түсті маталарды таңдап, суды көбірек ішіңіз.",
            "It is really hot today! Dress as lightly as possible, choose light-colored fabrics and drink more water.",
            "Está muito quente hoje! Vista-se o mais levemente possível, escolha tecidos de cores claras e beba mais água.",
            "Heute ist es wirklich heiß! Kleiden Sie sich so leicht wie möglich, wählen Sie helle Stoffe und trinken Sie mehr Wasser.",
            "Il fait vraiment chaud aujourd'hui ! Habillez-vous le plus légèrement possible, choisissez des tissus clairs et buvez plus d'eau.",
            "Fa davvero caldo oggi! Vestiti il più leggero possibile, scegli tessuti chiari e bevi più acqua.",
            "Bugun haqiqatan ham issiq! Iloji boricha yengil kiyining, ochiq rangli matolarni tanlang va ko'proq suv iching.",
            "Bugün gerçekten çok sıcak! Olabildiğince hafif giyinin, açık renkli kumaşlar seçin ve daha fazla su için.",
            "今天真是太热了！穿得尽可能轻薄，选择浅色面料，多喝水。",
            "Il fait vraiment chaud aujourd'hui ! Habille-toi le plus légèrement possible, choisis des tissus clairs et bois beaucoup d'eau.",
            "¡Hace mucho calor hoy! Vístete lo más ligero posible, elige telas de colores claros y bebe más agua.",
            "დღეს მართლაც ცხელა! ჩაიცვით რაც შეიძლება მსუბუქად, აირჩიეთ ღია ფერის ქსოვილები და დალიეთ მეტი წყალი.",
            "Hôm nay trời thực sự rất nóng! Hãy mặc đồ càng nhẹ nhàng càng tốt, chọn các loại vải sáng màu và uống nhiều nước hơn."
        ),
        "Знойная погода: обязательно захватите головной убор, бутылку прохладной воды и держитесь тени в полдень." to arrayOf(
            "Қаптаған ыстық: міндетті түрде бас киім, салқын су бөтелкесін алып, түсте көлеңкеде болыңыз.",
            "Sultry weather: be sure to grab a hat, a bottle of cool water and stay in the shade at noon.",
            "Tempo abafado: certifique-se de pegar um chapéu, uma garrafa de água fria e ficar na sombra ao meio-dia.",
            "Schwüles Wetter: Denken Sie unbedingt an eine Kopfbedeckung, eine Flasche kühles Wasser und halten Sie sich mittags im Schatten auf.",
            "Temps lourd : assurez-vous de prendre un chapeau, une bouteille d'eau fraîche et restez à l'ombre à midi.",
            "Tempo afoso: assicurati di prendere un cappello, una bottiglia di acqua fresca e resta all'ombra a mezzogiorno.",
            "Jazirama ob-havo: albatta bosh kiyim, salqin suvli shisha oling va tushda soyada bo'ling.",
            "Bunaltıcı hava: mutlaka bir şapka, bir şişe soğuk su alın ve öğlen saatlerinde gölgede kalın.",
            "闷热的天气：一定要戴上帽子，带上一瓶凉水，并在中午时分待在阴凉处。",
            "Un temps caniculaire : n'oublie pas de prendre un chapeau, une bouteille d'eau fraîche et de rester à l'ombre à midi.",
            "Clima bochornoso: asegúrate de llevar un sombrero, una botella de agua fresca y mantente a la sombra al mediodía.",
            "პაპანაქება სიცხე: აუცილებლად წაიღეთ ქუდი, გრილი წყლის ბოთლი და შუადღისას შეაფარეთ თავი ჩრდილს.",
            "Thời tiết oi bức: hãy chắc chắn mang theo một chiếc mũ, một chai nước mát và ở trong bóng râm vào buổi trưa."
        ),
        "Столбик термометра высоко — отдайте предпочтение свободным вещам из хлопка и льна и берегитесь палящего солнца." to arrayOf(
            "Термометр бағанасы жоғары — мақта мен зығырдан тігілген кең киімдерге басымдық беріңіз және күйдіретін күннен сақтаныңыз.",
            "The thermometer is high — give preference to loose clothes made of cotton and linen and beware of the scorching sun.",
            "O termômetro está alto — dê preferência a roupas largas de algodão e linho e cuidado com o sol escaldante.",
            "Das Thermometer steht hoch — bevorzugen Sie weite Kleidung aus Baumwolle und Leinen und hüten Sie sich vor der sengenden Sonne.",
            "Le thermomètre est élevé - privilégiez les vêtements amples en coton et en lin et méfiez-vous du soleil brûlant.",
            "Il termometro è alto: preferisci abiti larghi in cotone e lino e fai attenzione al sole cocente.",
            "Termometr ustuni yuqori — paxta va zig'irdan tikilgan erkin kiyimlarni afzal ko'ring va yondiruvchi quyoshdan saqlaning.",
            "Termometre yüksekte — pamuklu ve keten bol giysileri tercih edin ve kavurucu güneşten sakının.",
            "温度计显示高温——优先选择由棉和亚麻制成的宽松衣服，并当心烈日。",
            "Le mercure grimpe — privilégie les vêtements amples en coton et en lin et méfie-toi du soleil de plomb.",
            "El termómetro está alto: prefiere ropa holgada de algodón y lino y ten cuidado con el sol abrasador.",
            "თერმომეტრის სვეტი მაღლაა — უპირატესობა მიანიჭეთ თავისუფალ ტანსაცმელს ბამბისა და სელისგან და მოერიდეთ მცხუნვარე მზეს.",
            "Nhiệt kế đang ở mức cao — hãy ưu tiên mặc quần áo rộng rãi bằng cotton và lanh và cẩn thận với ánh nắng chói chang."
        ),
        "Приятная погода для прогулок и повседневных дел — одевайтесь по сезону и проведите этот день с комфортом." to arrayOf(
            "Серуендеуге және күнделікті істерге жағымды ауа райы — маусымға сай киініп, бұл күнді жайлы өткізіңіз.",
            "Pleasant weather for walks and everyday activities — dress for the season and spend this day in comfort.",
            "Clima agradável para caminhadas e atividades cotidianas — vista-se de acordo com a estação e passe este dia com conforto.",
            "Angenehmes Wetter für Spaziergänge und alltägliche Aktivitäten — kleiden Sie sich der Jahreszeit entsprechend und verbringen Sie diesen Tag in Komfort.",
            "Temps agréable pour les promenades et les activités quotidiennes - habillez-vous pour la saison et passez cette journée confortablement.",
            "Tempo piacevole per passeggiate e attività quotidiane: vestiti per la stagione e trascorri questa giornata in tutta comodità.",
            "Sayr qilish va kundalik ishlar uchun yoqimli ob-havo — mavsumga mos kiyining va bu kunni qulay o'tkazing.",
            "Yürüyüşler ve günlük aktiviteler için hoş bir hava — mevsime uygun giyinin ve bu günü rahat geçirin.",
            "适合散步和日常活动的宜人天气——根据季节着装，舒适地度过这一天。",
            "Un temps agréable pour les promenades et les activités quotidiennes — habille-toi selon la saison et passe cette journée confortablement.",
            "Clima agradable para caminar y realizar actividades cotidianas: vístete para la temporada y pasa este día con comodidad.",
            "სასიამოვნო ამინდი სეირნობისთვის და ყოველდღიური საქმეებისთვის — ჩაიცვით სეზონის შესაბამისად და გაატარეთ ეს დღე კომფორტულად.",
            "Thời tiết dễ chịu để đi dạo và thực hiện các hoạt động hàng ngày - hãy ăn mặc theo mùa và tận hưởng ngày hôm nay thật thoải mái."
        )
    )

    fun matchesQuery(cityNameRu: String, countryRu: String, rawQuery: String): Boolean {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) return false

        if (cityNameRu.lowercase().contains(q) || countryRu.lowercase().contains(q)) {
            return true
        }

        dictionary[cityNameRu]?.forEach { trans ->
            if (trans.lowercase().contains(q)) return true
        }
        dictionary[countryRu]?.forEach { trans ->
            if (trans.lowercase().contains(q)) return true
        }

        return false
    }

    fun translate(text: String): String {
        if (text.isBlank() || currentLanguage == "ru" || "Welcome to WeatherApp".startsWith(text) || (text.endsWith("°C") && !text.contains(":"))) {
            return text
        }

        val langIndex = langOrder.indexOf(currentLanguage)
        if (langIndex == -1) return text

        if (text.startsWith("Ощущается как:") && text != "Ощущается как:") {
            val actualTemp = text.removePrefix("Ощущается как:").trim()
            val prefix = dictionary["Ощущается как:"]?.getOrNull(langIndex) ?: "Ощущается как:"
            return "$prefix $actualTemp"
        }

        val translations = dictionary[text]
        if (translations != null && langIndex < translations.size) {
            return translations[langIndex]
        }

        return text
    }

    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_translator_cache", Context.MODE_PRIVATE)
        currentLanguage = prefs?.getString("current_language", "ru") ?: "ru"
    }

    fun setLanguage(context: Context, langCode: String) {
        currentLanguage = langCode
        prefs?.edit()?.putString("current_language", langCode)?.apply()
    }

    fun showTranslatedToast(context: Context, messageRu: String) {
        Toast.makeText(context, translate(messageRu), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun tr(text: String): String {
    return AppTranslator.translate(text)
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    androidx.compose.material3.Text(
        text = tr(text),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        maxLines = maxLines
    )
}

@Composable
fun LanguageSelectorButton(containerColor: Color = Color(0xFF455A64)) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLangObj = AppTranslator.languages.find { it.code == AppTranslator.currentLanguage } ?: AppTranslator.languages.first()
    val context = LocalContext.current

    Button(
        onClick = { showLanguageDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("${tr("Язык")}: ${currentLangObj.nativeTitle}", color = Color.White)
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(tr("Выберите язык")) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                    items(AppTranslator.languages) { lang ->
                        TextButton(
                            onClick = {
                                AppTranslator.setLanguage(context, lang.code)
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(lang.titleRu)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}