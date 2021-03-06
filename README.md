# orm4j8

## 概要

orm4j8はJava8向けのORマッピングライブラリです。
現在はMySQL、PostgreSQLに対応しています。

## 機能

* 基本的なCRUD
* 主キー検索
* メソッドチェーンによるクエリ生成
* HasOne
* HasMany

## 導入方法

### gradleプロジェクト

* repositoriesブロック内に下記を追加
```groovy
maven {
	url 'https://github.com/417-72KI/orm4j8/raw/master/repos'
}
```
* dependenciesブロック内に下記を追加
```groovy
compile 'jp.natsukishina:orm4j8:1.0.0'
```

### Maven

* repositoriesブロック内に下記を追加

```xml
<repository>
	<id>orm4j8</id>
	<url>https://github.com/417-72KI/orm4j8/raw/master/repos/</url>
</repository>
```
* dependenciesブロック内に下記を追加
```xml
<dependency>
	<groupId>jp.natsukishina</groupId>
	<artifactId>orm4j8</artifactId>
	<version>1.0.0</version>
</dependency>
```

### 一般的なJavaプロジェクト
* https://github.com/417-72KI/orm4j8/raw/master/repos/jp/natsukishina/orm4j8/orm4j8/1.0.0/orm4j8-1.0.0.jar
  をダウンロードし、ビルドパスに加える

## その他必要なライブラリ

* MySQLを使用する場合  mysql-connector-java-*-bin.jar
* PostgreSQLを使用する場合  postgresql-*.jar

## TODO

* PostgreSQLでの動作確認
* Oracle対応
* cascade対応

## JavaDoc

[Ver 1.0.0](http://417-72ki.github.io/orm4j8/javadoc/1.0.0/)

## ライセンス

Copyright &copy; 2016 417.72KI
Licensed under the [MIT License][mit].

[MIT]: http://www.opensource.org/licenses/mit-license.php
