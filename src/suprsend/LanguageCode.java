package suprsend;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageCode {
    private static final String[][] langCodesArr =  new String[][]{
        {"aa", "Afar"},
        {"ab", "Abkhazian"},
        {"ae", "Avestan"},
        {"af", "Afrikaans"},
        {"ak", "Akan"},
        {"am", "Amharic"},
        {"an", "Aragonese"},
        {"ar", "Arabic"},
        {"as", "Assamese"},
        {"av", "Avaric"},
        {"ay", "Aymara"},
        {"az", "Azerbaijani"},
        {"ba", "Bashkir"},
        {"be", "Belarusian"},
        {"bg", "Bulgarian"},
        {"bi", "Bislama"},
        {"bm", "Bambara"},
        {"bn", "Bengali"},
        {"bo", "Tibetan"},
        {"br", "Breton"},
        {"bs", "Bosnian"},
        {"ca", "Catalan; Valencian"},
        {"ce", "Chechen"},
        {"ch", "Chamorro"},
        {"co", "Corsican"},
        {"cr", "Cree"},
        {"cs", "Czech"},
        {"cu", "Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic"},
        {"cv", "Chuvash"},
        {"cy", "Welsh"},
        {"da", "Danish"},
        {"de", "German"},
        {"dv", "Divehi; Dhivehi; Maldivian"},
        {"dz", "Dzongkha"},
        {"ee", "Ewe"},
        {"el", "Greek, Modern (1453-)"},
        {"en", "English"},
        {"eo", "Esperanto"},
        {"es", "Spanish; Castilian"},
        {"et", "Estonian"},
        {"eu", "Basque"},
        {"fa", "Persian"},
        {"ff", "Fulah"},
        {"fi", "Finnish"},
        {"fj", "Fijian"},
        {"fo", "Faroese"},
        {"fr", "French"},
        {"fy", "Western Frisian"},
        {"ga", "Irish"},
        {"gd", "Gaelic; Scottish Gaelic"},
        {"gl", "Galician"},
        {"gn", "Guarani"},
        {"gu", "Gujarati"},
        {"gv", "Manx"},
        {"ha", "Hausa"},
        {"he", "Hebrew"},
        {"hi", "Hindi"},
        {"ho", "Hiri Motu"},
        {"hr", "Croatian"},
        {"ht", "Haitian; Haitian Creole"},
        {"hu", "Hungarian"},
        {"hy", "Armenian"},
        {"hz", "Herero"},
        {"ia", "Interlingua (International Auxiliary Language Association)"},
        {"id", "Indonesian"},
        {"ie", "Interlingue; Occidental"},
        {"ig", "Igbo"},
        {"ii", "Sichuan Yi; Nuosu"},
        {"ik", "Inupiaq"},
        {"io", "Ido"},
        {"is", "Icelandic"},
        {"it", "Italian"},
        {"iu", "Inuktitut"},
        {"ja", "Japanese"},
        {"jv", "Javanese"},
        {"ka", "Georgian"},
        {"kg", "Kongo"},
        {"ki", "Kikuyu; Gikuyu"},
        {"kj", "Kuanyama; Kwanyama"},
        {"kk", "Kazakh"},
        {"kl", "Kalaallisut; Greenlandic"},
        {"km", "Central Khmer"},
        {"kn", "Kannada"},
        {"ko", "Korean"},
        {"kr", "Kanuri"},
        {"ks", "Kashmiri"},
        {"ku", "Kurdish"},
        {"kv", "Komi"},
        {"kw", "Cornish"},
        {"ky", "Kirghiz; Kyrgyz"},
        {"la", "Latin"},
        {"lb", "Luxembourgish; Letzeburgesch"},
        {"lg", "Ganda"},
        {"li", "Limburgan; Limburger; Limburgish"},
        {"ln", "Lingala"},
        {"lo", "Lao"},
        {"lt", "Lithuanian"},
        {"lu", "Luba-Katanga"},
        {"lv", "Latvian"},
        {"mg", "Malagasy"},
        {"mh", "Marshallese"},
        {"mi", "Maori"},
        {"mk", "Macedonian"},
        {"ml", "Malayalam"},
        {"mn", "Mongolian"},
        {"mr", "Marathi"},
        {"ms", "Malay"},
        {"mt", "Maltese"},
        {"my", "Burmese"},
        {"na", "Nauru"},
        {"nb", "Bokmål, Norwegian; Norwegian Bokmål"},
        {"nd", "Ndebele, North; North Ndebele"},
        {"ne", "Nepali"},
        {"ng", "Ndonga"},
        {"nl", "Dutch; Flemish"},
        {"nn", "Norwegian Nynorsk; Nynorsk, Norwegian"},
        {"no", "Norwegian"},
        {"nr", "Ndebele, South; South Ndebele"},
        {"nv", "Navajo; Navaho"},
        {"ny", "Chichewa; Chewa; Nyanja"},
        {"oc", "Occitan (post 1500)"},
        {"oj", "Ojibwa"},
        {"om", "Oromo"},
        {"or", "Oriya"},
        {"os", "Ossetian; Ossetic"},
        {"pa", "Panjabi; Punjabi"},
        {"pi", "Pali"},
        {"pl", "Polish"},
        {"ps", "Pushto; Pashto"},
        {"pt", "Portuguese"},
        {"qu", "Quechua"},
        {"rm", "Romansh"},
        {"rn", "Rundi"},
        {"ro", "Romanian; Moldavian; Moldovan"},
        {"ru", "Russian"},
        {"rw", "Kinyarwanda"},
        {"sa", "Sanskrit"},
        {"sc", "Sardinian"},
        {"sd", "Sindhi"},
        {"se", "Northern Sami"},
        {"sg", "Sango"},
        {"si", "Sinhala; Sinhalese"},
        {"sk", "Slovak"},
        {"sl", "Slovenian"},
        {"sm", "Samoan"},
        {"sn", "Shona"},
        {"so", "Somali"},
        {"sq", "Albanian"},
        {"sr", "Serbian"},
        {"ss", "Swati"},
        {"st", "Sotho, Southern"},
        {"su", "Sundanese"},
        {"sv", "Swedish"},
        {"sw", "Swahili"},
        {"ta", "Tamil"},
        {"te", "Telugu"},
        {"tg", "Tajik"},
        {"th", "Thai"},
        {"ti", "Tigrinya"},
        {"tk", "Turkmen"},
        {"tl", "Tagalog"},
        {"tn", "Tswana"},
        {"to", "Tonga (Tonga Islands)"},
        {"tr", "Turkish"},
        {"ts", "Tsonga"},
        {"tt", "Tatar"},
        {"tw", "Twi"},
        {"ty", "Tahitian"},
        {"ug", "Uighur; Uyghur"},
        {"uk", "Ukrainian"},
        {"ur", "Urdu"},
        {"uz", "Uzbek"},
        {"ve", "Venda"},
        {"vi", "Vietnamese"},
        {"vo", "Volapük"},
        {"wa", "Walloon"},
        {"wo", "Wolof"},
        {"xh", "Xhosa"},
        {"yi", "Yiddish"},
        {"yo", "Yoruba"},
        {"za", "Zhuang; Chuang"},
        {"zh", "Chinese"},
        {"zu", "Zulu"}
    };

    public static final Map<String, String> LANG_CODE_NAME_MAPPING = Stream.of(langCodesArr).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public static final Set<String> ALL_LANG_CODES = LANG_CODE_NAME_MAPPING.keySet();

}
