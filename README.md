# VioletCore

VioletCore 26.2 is a Purpur 26.2 based server software prototype with an early Engine Plugin layer.

The runnable server jar is uploaded as a GitHub Release asset because it is larger than GitHub's normal git file size limit.

## Run

```bash
java -Xmx2G -jar VioletCore-26.2.local-SNAPSHOT.jar --nogui
```

## Engine Plugins

Default folder:

```text
engine-plugins/
```

Run with Engine Plugins:

```bash
java -Xmx2G -jar VioletCore-26.2.local-SNAPSHOT.jar --nogui --engine-plugins-dir engine-plugins
```
