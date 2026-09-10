package com.teamdark.ai

/**
 * TEAMDARK.AI offline engine — 100% on-device, no API, unlimited.
 * Coding specialist: C++, Java, Python, Lua, APK/Mod basics.
 * Helpful + open for learning, with basic safety for harmful requests.
 */
object TeamDarkAIEngine {

    fun reply(raw: String, hasImage: Boolean): String {
        val q = raw.trim()
        val l = q.lowercase()

        if (q.isEmpty() && hasImage) {
            return "📸 Photo mil gayi! Main offline hoon isliye image ko pixels me analyze nahi kar sakta, " +
                    "lekin batao is photo ke sath kya karna hai?\n\n" +
                    "• Is se related code chahiye?\n• UI copy karna hai?\n• Ya ispe koi edit / idea chahiye?\n\nDetail likho, main turant code ya steps de dunga."
        }

        if (l.isEmpty()) return "Kuch likho ya photo bhejo — main ready hoon. C++, Java, Python, Lua, Mod me se kuch try karo."

        // Greetings
        if (match(l, listOf("hello", "hi", "hey", "namaste", "salam", "assalam", "yo", "ram ram"))) {
            return "😎 Yo! Main hoon **TEAMDARK.AI** — offline, unlimited, smooth.\n\n" +
                    "Main inme master hoon:\n" +
                    "• C++ • Java • Python • Lua\n• APK structure • Mod basics\n\n" +
                    "Batao kya banwana hai? Example: `python me photo downloader banao`, `c++ me aim practice code do`."
        }

        if (l.contains("tum kaun") || l.contains("who are you") || l.contains("tera naam") || l.contains("apna intro")) {
            return "🤖 Main **TEAMDARK.AI** hoon — TeamDark ka offline AI.\n" +
                    "• No API, no limit, no net needed\n• Coding + mod learning specialist\n• Made with style, Powered by DARK DEVEL\n\n" +
                    "Bas bolo kya chahiye — code, explain, ya project idea?"
        }

        // Safety: multiplayer cheats / bypass
        if (containsAny(l, listOf("aimbot", "esp", "wallhack", "bypass", "obbypass", "uc hack", "diamond hack", "unlimited uc", "injector", "antiban", "ban bypass", "pubg hack", "free fire hack", "8 ball hack", "pool hack", "long line", "auto win"))) {
            return "⚠️ Ye wali cheez main directly nahi de sakta — multiplayer hacks / bypass se dusre players ka game kharab hota hai aur ID ban ka risk rehta hai.\n\n" +
                    "Lekin main tumhe **legit tareeke se master** bana sakta hoon:\n" +
                    "• C++/Java me game logic kaise kaam karta hai\n• Apna khud ka offline practice app kaise banaye\n• Lua scripting, UI, mod-menu design basics\n\n" +
                    "Bolo — `C++ seekhna hai`, `Java me mod menu UI banana hai`, ya `Lua basics`? Waha se start karte hain."
        }

        // Disallowed: hate/illegal quick guard
        if (containsAny(l, listOf("bomb banana", "weapon banana", "hack karna sikh", "kisi ka account hack", "password hack", "carding", "phishing"))) {
            return "❌ Is topic me main help nahi kar sakta — illegal / harmful hai.\n\n" +
                    "Iske bajaye main sikha sakta hoon:\n• Ethical coding • App development • Python automation • Game dev basics\nBatao kaunsi side chale?"
        }

        // C++
        if (l.contains("c++") || l.contains("cpp")) {
            if (l.contains("mod menu") || l.contains("modmenu") || l.contains("menu")) {
                return cppModMenu()
            }
            if (l.contains("aim") || l.contains("practice") || l.contains("game")) {
                return "🎯 C++ Aim Practice (offline console):\n\n" +
                        "```cpp\n#include <iostream>\n#include <cstdlib>\n#include <ctime>\nusing namespace std;\n\nint main() {\n  srand(time(0));\n  int score=0;\n  for(int i=1;i<=5;i++){\n    int target = rand()%9+1;\n    int guess;\n    cout << \"Round \" << i << \" - Target 1-9 guess karo: \";\n    cin >> guess;\n    if(guess==target){ cout << \"HIT!\\n\"; score++; }\n    else cout << \"MISS! tha \" << target << \"\\n\";\n  }\n  cout << \"Score: \" << score << \"/5\\n\";\n}\n```\n\nMaster tip: `rand`, `loop`, `input` — ye 3 pakad liye to game logic easy. Agla step chahiye to bolo `c++ me health system banao`."
            }
            return "💻 C++ Master Template:\n\n```cpp\n#include <iostream>\nusing namespace std;\n\n// TeamDark style function\nvoid darkPrint(string msg){\n  cout << \"[TD] \" << msg << endl;\n}\n\nint main(){\n  darkPrint(\"TEAMDARK.AI ready\");\n  int n; cout << \"Number do: \"; cin >> n;\n  for(int i=1;i<=10;i++)\n    cout << n << \" x \" << i << \" = \" << n*i << endl;\n  return 0;\n}\n```\n\nBatao isko kis me badlu — `file handling`, `class`, `game loop` ya `mod menu base`?"
        }

        // Java
        if (l.contains("java")) {
            if (l.contains("mod") || l.contains("menu") || l.contains("android") || l.contains("apk")) {
                return "📱 Java Android Mod-Menu UI (basic learning):\n\n" +
                        "```java\n// MainActivity.java - floating button example\nimport android.os.Bundle;\nimport android.widget.*;\nimport androidx.appcompat.app.AppCompatActivity;\n\npublic class MainActivity extends AppCompatActivity {\n  boolean featureON = false;\n  @Override protected void onCreate(Bundle s){\n    super.onCreate(s);\n    LinearLayout lay = new LinearLayout(this);\n    lay.setOrientation(LinearLayout.VERTICAL);\n    lay.setPadding(40,40,40,40);\n    TextView t = new TextView(this);\n    t.setText(\"TEAMDARK Mod UI\");\n    t.setTextSize(22);\n    Button b = new Button(this);\n    b.setText(\"Feature: OFF\");\n    b.setOnClickListener(v->{\n      featureON = !featureON;\n      b.setText(featureON?\"Feature: ON\":\"Feature: OFF\");\n    });\n    lay.addView(t); lay.addView(b);\n    setContentView(lay);\n  }\n}\n```\n\nYe sirf UI learning hai. Real mod me smali/lib ka concept lagta hai — bolo `apk structure samjhao` to pura map deta hoon."
            }
            return "☕ Java Starter (master level):\n\n```java\npublic class TeamDark {\n  public static void main(String[] args){\n    System.out.println(\"[TD] TEAMDARK.AI Java ready\");\n    for(int i=1;i<=5;i++)\n      System.out.println(\"Level \"+i+\" unlocked\");\n  }\n}\n```\n\nNext: `java me login system`, `java me list`, ya `android button`? Bolo."
        }

        // Python
        if (l.contains("python") || l.contains("py")) {
            if (l.contains("photo") || l.contains("image") || l.contains("gallery") || l.contains("download")) {
                return "🐍 Python Photo/Files Tool:\n\n" +
                        "```python\nimport os\nfrom pathlib import Path\n\n# TeamDark style organizer\nfolder = Path(\"./photos\")\nfolder.mkdir(exist_ok=True)\nprint(\"[TD] Folder ready:\", folder.resolve())\n\n# apni photos ka naam list karo\nfor f in Path(\".\").glob(\"*.jpg\"):\n    print(\"Found:\", f.name)\n\nprint(\"Done! Yehi logic Android me gallery picker banta hai.\")\n```\n\nAur chahiye to bolo `python me encrypt tool`, `python me apk parser`."
            }
            if (l.contains("encrypt") || l.contains("decrypt") || l.contains("tool")) {
                return "🔐 Python Encrypt Tool (tumhare DARK-Python-Encrypt-Tool style):\n\n" +
                        "```python\n# simple xor demo - learning only\ndef xor_crypt(data: bytes, key: bytes) -> bytes:\n    return bytes(b ^ key[i % len(key)] for i, b in enumerate(data))\n\nmsg = input(\"Text do: \").encode()\nkey = b\"TEAMDARK\"\nenc = xor_crypt(msg, key)\nprint(\"Encrypted:\", enc.hex())\ndec = xor_crypt(enc, key)\nprint(\"Decrypted:\", dec.decode())\n```\n\nReal APK/SO encrypt me aur layers lagti hain. Bolo to `advance version` dun."
            }
            return "🐍 Python Master Snippet:\n\n```python\n# teamdark.py\ndef dark_ai(task):\n    print(f\"[TD] Working on: {task}\")\n    steps = [\"plan\", \"code\", \"test\", \"polish\"]\n    for i,s in enumerate(steps,1):\n        print(f\"{i}. {s} done\")\n\nif __name__ == \"__main__\":\n    dark_ai(input(\"Task batao: \"))\n```\n\nBolo — `automation`, `file tool`, ya `kivy app`?"
        }

        // Lua
        if (l.contains("lua")) {
            return "🌙 Lua Master (mod scripting king):\n\n" +
                    "```lua\n-- teamdark.lua\nTD = { name = \"TEAMDARK.AI\", ver = \"1.0\" }\n\nfunction TD.greet(user)\n  print(\"[TD] Hello \" .. user .. \"!\")\nend\n\nfunction TD.modToggle(feature, on)\n  if on then\n    print(feature .. \" -> ON\")\n  else\n    print(feature .. \" -> OFF\")\n  end\nend\n\nTD.greet(\"Boss\")\nTD.modToggle(\"Speed\", true)\nTD.modToggle(\"Wall\", false)\n```\n\nLua me `GameGuardian script`, `UI`, ya `loop` seekhna hai? Bolo."
        }

        // APK / Mod structure
        if (containsAny(l, listOf("apk", "smali", "mod", "decompile", "lib.so", "mod menu", "structure"))) {
            return "📦 APK / Mod Map (learning roadmap):\n\n" +
                    "1. APK = ZIP (AndroidManifest.xml + classes.dex + res/ + lib/*.so)\n" +
                    "2. Tools: APKTool (decode), JADX (java dekhna), MT Manager (mobile)\n" +
                    "3. Smali = dex ka assembly — logic yahi badalta hai\n" +
                    "4. lib.so = C++ native — IDA/Ghidra se analysis\n" +
                    "5. Mod Menu = floating UI + switch → memory/function hook\n\n" +
                    "Safe practice: apni khud ki debug APK banao aur uspe try karo.\nBolo `smali example do` ya `lib.so kya hota hai`?"
        }

        if (l.contains("smali")) {
            return "🧩 Smali Mini Example:\n\n" +
                    "```smali\n# Java: if(score > 100) unlock();\nconst/16 v0, 0x64\nif-le v1, v0, :locked\ninvoke-virtual {p0}, Lcom/game;->unlock()V\n:locked\nreturn-void\n```\n\nSmali me `const`, `if-`, `invoke` — bas ye 3 master kar lo. Practice ke liye apni app decode karo."
        }

        if (hasImage) {
            return "📸 Photo ke sath tumhara msg: \"" + q + "\"\n\n" +
                    "Main offline hoon, isliye photo ka deep analysis nahi kar sakta, lekin is base pe help kar dunga:\n" +
                    "• Agar UI ka screenshot hai → us jaisa XML layout bana dunga\n• Agar error ka photo hai → error text likh do, fix dunga\n• Agar code ka photo hai → code type karke bhejo, optimize kar dunga\n\n" +
                    "Batao photo me kya hai? 1 line me likho."
        }

        if (l.contains("thank") || l.contains("shukriya") || l.contains("nice") || l.contains("mast") || l.contains("op")) {
            return "😍 Dil khush kar diya! Style to TeamDark ka hai hi.\n\nAur kuch banwana hai? `naya feature`, `naya code`, ya `UI aur stylish`? Bolo boss."
        }

        if (l.contains("copy") || l.contains("gallery") || l.contains("photo bhej")) {
            return "✨ Features ready hain:\n\n• **COPY**: mere har reply ke neeche ⧉ COPY dabao — turant copy ho jayega\n• **GALLERY**: neeche 🖼 dabao → photo select karo → send dabao\n• **UNLIMITED**: no API, no limit, jitna chaho chat karo\n\nTry karo — koi code bolo aur copy dabake dekho."
        }

        // Default smart fallback
        return smartDefault(q)
    }

    private fun smartDefault(q: String): String {
        return "🔥 Samajh gaya: \"" + takePreview(q) + "\"\n\n" +
                "Main offline master hoon — ispe best help ye de sakta hoon:\n\n" +
                "1. Agar **code** chahiye to language ke sath bolo:\n" +
                "   • `c++ me ___ banao`\n   • `java me ___ banao`\n   • `python me ___ banao`\n   • `lua me ___ banao`\n" +
                "2. Agar **mod/APK** seekhna hai to bolo `apk structure samjhao`\n" +
                "3. Agar **photo** related hai to 🖼 se photo bhejo + 1 line likho\n\n" +
                "Example try karo: `python me file organizer banao`"
    }

    private fun takePreview(s: String): String {
        return if (s.length > 80) s.substring(0, 80) + "…" else s
    }

    private fun match(l: String, words: List<String>): Boolean {
        val t = " $l "
        return words.any { t.contains(" $it ") || t.contains(" $it,") || l == it || l.startsWith("$it ") }
    }

    private fun containsAny(l: String, words: List<String>): Boolean {
        return words.any { l.contains(it) }
    }

    private fun cppModMenu(): String {
        return "🧬 C++ Mod-Menu Base (learning concept):\n\n" +
                "```cpp\n#include <iostream>\n#include <string>\n#include <map>\n#include <vector>\nusing namespace std;\n\nmap<string,bool> F = {{\"Speed\",false},{\"Jump\",false},{\"Glow\",false}};\n\nvoid toggle(string k){\n  F[k]=!F[k];\n  cout << k << \" -> \" << (F[k]?\"ON\":\"OFF\") << endl;\n}\nvoid show(){\n  cout << \"\\n==== TEAMDARK MENU ====\\n\";\n  int i=1; for(auto &p:F) cout << i++ << \". \" << p.first << \" [\" << (p.second?\"ON\":\"OFF\") << \"]\\n\";\n  cout << \"0. Exit\\nChoice: \";\n}\nint main(){\n  int c;\n  vector<string> keys={\"Speed\",\"Jump\",\"Glow\"};\n  while(true){ show(); cin>>c; if(c==0)break;\n    if(c>=1&&c<=3) toggle(keys[c-1]); }\n}\n```\n\nYe PC/console concept hai. Android real menu me Java/Kotlin floating UI + native hook lagta hai. Bolo `java me floating button` to UI dun."
    }
}
