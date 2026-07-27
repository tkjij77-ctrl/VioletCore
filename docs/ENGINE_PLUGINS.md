# Engine Plugins

Engine Plugins هي طبقة خاصة في VioletCore يتم تحميلها قبل plugins العادية.

## الفرق بينها وبين Bukkit/Paper Plugins

| النوع | مكان التحميل | القوة | Reload |
|---|---|---|---|
| Normal Plugin | `plugins/` | أوامر، أحداث، حماية، اقتصاد | ممكن حسب البلجن |
| Engine Plugin | `engine-plugins/` | Hooks داخلية مثل entity ticking | Restart فقط |

## المجلد الافتراضي

```text
engine-plugins/
```

تشغيل مع مجلد مخصص:

```bash
java -jar VioletCore.jar --nogui --engine-plugins-dir engine-plugins
```

## ملف metadata

كل Engine Plugin يحتاج ملف:

```text
engine-plugin.yml
```

مثال:

```yaml
name: ExampleTickObserver
version: 1.0.0
main: dev.violet.example.ExampleTickObserver
type: engine-plugin
target-server: VioletCore
target-version: 26.2
load-phase: pre-minecraft
reloadable: false
modifies:
  - tick-observer-demo
  - entity-ticking
conflicts: []
mixin-configs: []
```

## Version Lock

VioletCore يرفض أي Engine Plugin لا يطابق:

```yaml
target-server: VioletCore
target-version: 26.2
```

## Conflict Detection

لو Pluginين عدلوا نفس المنطقة:

```yaml
modifies:
  - entity-ticking
```

VioletCore يمنع التعارض.

## Hooks الحالية

### TickObserver

```java
context.registerTickObserver(new TickObserver() {
    @Override
    public void onServerTickStart(int tick) {}

    @Override
    public void onServerTickEnd(int tick, double tickDurationMillis, long remainingNanos) {}
});
```

### EntityTickController

```java
context.registerEntityTickController((entity, worldName, tick) -> {
    return true;  // اسمح للـ entity تعمل tick طبيعي
    // return false; // امنع tick للـ entity في هذه التكة
});
```

> تحذير: `return false` يغير gameplay وقد يكسر farms أو AI إذا استُخدم بعشوائية.

## Mixins

VioletCore MVP يقرأ `mixin-configs` في metadata لكنه لا يطبق Mixins بعد. دعم restricted Mixins مخطط لاحقًا.

## مثال جاهز

راجع:

```text
examples/engine-plugin/
```


## EngineStatsProvider

Engine Plugins may implement `EngineStatsProvider` to expose custom lines in:

```text
/violetcore stats
```

Example:

```java
public final class MyPlugin implements EnginePlugin, EngineStatsProvider {
    @Override
    public List<String> stats() {
        return List.of("my-counter=" + counter.sum());
    }

    @Override
    public void resetStats() {
        counter.reset();
    }
}
```

VioletCore prefixes provider lines with:

```text
provider.<PluginName>.
```
