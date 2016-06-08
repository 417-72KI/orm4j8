# orm4j8

## 概要

orm4j8はJava8向けのORマッピングライブラリです。
現在はMySQL、PostgreSQLに対応しています。

## 機能

準備中...

## 使い方

* repositoriesブロック内に下記を追加
```
maven {
	url 'https://github.com/417-72KI/CSVMapper/raw/master/repos'
}
```
* dependenciesブロック内に下記を追加
```
compile 'jp.natsukishina.orm4j8:orm4j8:1.0.0'
```

## 依存ライブラリ

* MySQLを使用する場合  mysql-connector-java-*-bin.jar
* PostgreSQLを使用する場合  postgresql-*.jar

## TODO

* PostgreSQLでの動作確認
* HasOneの実装
* Oracle対応

## ライセンス

Copyright &copy; 2016 417.72KI
Licensed under the [MIT License][mit].

[MIT]: http://www.opensource.org/licenses/mit-license.php
