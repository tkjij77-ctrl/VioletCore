# VioletCore Real Server Software Plan

## الهدف

تحويل VioletCore من مستودع باتشات وإصدارات جاهزة إلى **Fork حقيقي قابل للبناء** مثل Purpur/Paper.

النتيجة المطلوبة:

```bash
git clone https://github.com/tkjij77-ctrl/VioletCore
cd VioletCore
./gradlew applyAllPatches
./gradlew :purpur-server:createBundlerJar -x test
```

ويخرج JAR قابل للتشغيل.

---

## كيف Purpur مبني؟

Purpur يستخدم Paperweight patcher:

- `settings.gradle.kts` يعرّف المشاريع.
- `gradle.properties` يثبت `mcVersion` و `paperCommit`.
- `build.gradle.kts` يسحب Paper عند commit محدد ويطبق patch sets.
- `purpur-api/paper-patches` تعدل Paper API.
- `purpur-server/paper-patches` تعدل Paper server.
- `purpur-server/minecraft-patches` تعدل Minecraft sources.

VioletCore يجب أن يتبع نفس النموذج.

---

## المرحلة 1 — Full Fork Structure

- استخدام `PurpurMC/Purpur ver/26.2` كقاعدة كاملة.
- الاحتفاظ بـ Gradle wrapper و paperweight setup.
- إضافة VioletCore patches داخل نفس أماكن Purpur القياسية.
- استبدال README/docs بملفات VioletCore.
- حذف كل ما لا يخص السيرفر core.

## المرحلة 2 — Versioning

إضافة:

```properties
group=io.violetmc.violetcore
violetcoreVersion=0.5.0
channel=BETA
```

وتعديل version الناتج إلى:

```text
26.2-v0.5.0
```

## المرحلة 3 — Core Build Verification

- `./gradlew applyAllPatches`
- `./gradlew :purpur-api:compileJava :purpur-server:compileJava`
- `./gradlew :purpur-server:createBundlerJar -x test`
- smoke test server startup.

## المرحلة 4 — CI

GitHub Actions يبني السيرفر فعليًا:

- checkout
- setup Java 25
- applyAllPatches
- compile
- createBundlerJar
- upload artifact

## المرحلة 5 — Releases

كل release يحتوي:

- `VioletCore-26.2-vX.Y.Z.jar`
- `.sha256`
- source patches zip
- official engine plugin jars

## المرحلة 6 — Beta criteria

VioletCore يعتبر Beta عندما:

- المستودع يبني من source.
- CI ينجح.
- release pipeline يرفع JAR تلقائيًا.
- Engine Plugin API مستقرة نسبيًا.
- SmartEntityTick آمن افتراضيًا.
- docs للمطورين واضحة.

---

## قرار v0.5.0

v0.5.0 سيكون أول إصدار من VioletCore كمستودع full fork buildable، مع الإبقاء مؤقتًا على أسماء modules:

```text
purpur-api
purpur-server
```

وسيتم تأجيل إعادة تسمية modules إلى:

```text
violetcore-api
violetcore-server
```

لإصدار لاحق لتقليل المخاطر.
