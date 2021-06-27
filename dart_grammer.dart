// main function & print() function

void main() {
  print('Hello, World!');
}

// 변수 선언
// var는 변수 type을 일일이 정의할 필요 없이 자기가 자동적으로 분류해서 저장

var name = 'Voyager I';
var year = 1977;
var antennaDiameter = 3.7;
var flybyObjects = ['Jupiter', 'Saturn', 'Uranus', 'Neptune'];
var image = {
  'tags': ['saturn'],
  'url': '//path/to/saturn.jpg'
};

// final, const : 상수 선언
// 상수는 값을 한 번 초기화 해놓으면 다른 값으로 변경이 불가능

void main() {
  const int cnt = 20;
  // cnt = 30; 입력 시 에러 발생

  final String str = "Dart Language";
  // str =  Language"; // 에러 발생
}


// 제어문

if (year >= 2001) {
  print('21st century');
} else if (year >= 1901) {
  print('20th century');
}

for (var object in flybyObjects) {
  print(object);
}

for (int month = 1; month <= 12; month++) {
  print(month);
}

while (year < 2016) {
  year += 1;
}


// 함수
// 함수의 type을 구체적으로 설정하는 것을 권장

int fibonacci(int n) {
  if (n == 0 || n == 1) return n;
  return fibonacci(n - 1) + fibonacci(n - 2);
}

var result = fibonacci(20);


// imports

// Importing core libraries
import 'dart:math';

// Importing libraries from external packages
import 'package:test/test.dart';


// Importing files
import 'path/to/my_other_file.dart';


// 클래스

class Spacecraft {
  String name;
  DateTime? launchDate;

  int? get launchYear => launchDate?.year; // read-only non-final property

  // Constructor, with syntactic sugar for assignment to members.
  Spacecraft(this.name, this.launchDate) {
    // Initialization code goes here.
  }

  // Named constructor that forwards to the default one.
  Spacecraft.unlaunched(String name) : this(name, null);

  // Method.
  void describe() {
    print('Spacecraft: $name');
    var launchDate = this.launchDate; // Type promotion doesn't work on getters.
    if (launchDate != null) {
      int years = DateTime.now().difference(launchDate).inDays ~/ 365;
      print('Launched: $launchYear ($years years ago)');
    } else {
      print('Unlaunched');
    }
  }
}


// 상속

class Orbiter extends Spacecraft {
  double altitude;

  Orbiter(String name, DateTime launchDate, this.altitude)
      : super(name, launchDate);
}


// interfaces and abstract classes
// Dart는 interface 키워드가 없음. 모든 클래스 종류가 인터페이스에 명확히 정의되어 있음.
// 그러므로 어떤 클래스든 실행 가능함.

class MockSpaceship implements Spacecraft {
  // ···
}

// abstract class 는 아직 정해지지 않은 클래스로, 완성된 클래스를 실행하거나
// 완성된 클래스를 통해 확장 가능
// 완성되지 않은 실행문 작성 가능
// 대충 봤을 때, 아직 덜 만든 클래스 쓸 때 abstract class로 구현하면 될 거 같다.

abstract class Describable {
  void describe();

  void describeWithEmphasis() {
    print('=========');
    describe();
    print('=========');
  }
}


// 예외 처리

if (astronauts == 0) {
  throw StateError('No astronauts.');
}

// 에러 발생시키기

try {
  for (var object in flybyObjects) {
    var description = await File('$object.txt').readAsString();
    print(description);
  }
} on IOException catch (e) {
  print('Could not describe object: $e');
} finally {
  flybyObjects.clear();
}

// try 구문 사용은 다른 언어와 비슷핟 것 같다