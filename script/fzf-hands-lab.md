## 실습 스크립트

### 시작 부분

혹시, 저장소 클론 및 stash 세팅이 다 되셨을까요??


실습 시작하기에 앞서, 로컬에서 stash 목록을 만들기 위해서 스크립트를 실행해야 합니다.

```shell
chmod +x ./setup-stash.sh && ./setup-stash.sh
```

해당 명령어를 입력해주세요.
실행권한을 추가하고, 파일을 실행해 stash 목록을 생성해줍니다.

다 실행하셨을까요?

### step1

Hello-World
input-name
case-command

작성하기!!!!

```shell
cd step/step1
```

네. step1 폴더로 이동하겠습니다.
가장 기초인 hello-world 부터 함수를 작성할텐데요.

```shell
vi hello-world.sh
```

라고 입력해주세요.
파일 편집기를 연다는 의미입니다.

```shell
hello-world() {
 echo "Hello World"  
}
```

hello-world 라는 이름으로 함수를 선언합니다.
그리고, `echo "Hello World"` 라고 입력합니다.

echo 는 다른 프로그래밍언어에서 출력문을 의미합니다.

이제 파일 작성이 끝났으니
Esc 를 누르고, `Shift + ;` 누르고 wq 를 입력해주세요.
파일 작성을 마치고 저장한다는 의미입니다.

작성을 마쳤으니 함수를 터미널에 불러와야 하는데요.
함수를 불러오지 않고 먼저

```shell
hello-world
```

를 실행해보면 command_not_found 가 뜨는걸 확인할 수 있습니다.

```shell
source hello-world
```

이제 hello-world! 라는 출력문이 제대로 나오는것을 확인할 수 있습니다.

두번째로, 입력을 받아보겠습니다.

```shell
vi input-name.sh
```

input-name.sh 이라는 이름으로 만들게요.

```shell
input-name() {
  local name
  echo "이름을 입력해주세요"
  read name
  echo "이름은 $name 입니다."
}
```

함수 내부에서 변수를 선언할때는 `local` 키워드를 사용합니다.
read 는 입력값을 받아서 변수에 값을 할당해줍니다.

함수를 다 작성했으니, 파일 작성을 마치고 저장할게요.

```shell
source input-name
```

함수를 불러오고

```shell
input-name
```

함수를 실행해보겠습니다.
이름을 입력해주세요 라는 출력이 보이고

이름을 입력하면
`이름은 이영수 입니다.` 와 같이 출력되는걸 볼 수 있습니다.

세번째로, switch 구문입니다.

```shell
vi case-command.sh
```

이번에는 case-command 라는 이름으로 함수를 작성해보겠습니다.

```shell
case-command() {
  echo "command 를 입력해주세요"
  local command
  read command
  command=${command:0:1}
  case "$command" in
    a|A)
      echo "apply 가 선택되었습니다"
      ;;
    p|P)
      echo "pop 이 선택되었습니다"
      ;;
    d|D)
      echo "drop 이 선택되었습니다"
      ;;
    *)
      echo "아무것도 선택되지 않았습니다"
  esac
}
```

기존과 동일하게
echo "command 를 입력해주세요" 라고 입력하고
변수 선언 및 read 구문을 작성할게요.

`${command:0:1}` 구문은 문장을 받으면 맨 앞 글자만 자르는걸 의미합니다.

다음 case 구문은

```shell
case "$command" in
```

의 문법으로 시작하고

```shell
a|A) echo "apply 가 선택되었습니다" ;;
```

이때, 주의점으로는 `;;` 를 작성해서
이 case 가 끝났다는걸 알려줘야 합니다.

이렇게 step1 이 끝났습니다.

저희는 터미널에서

- hello world
- 입력값 받기
- 문장 자르기

를 해봤습니다. 이를 활용해서, 함수 내부에서 값을 입력, 출력, 조작을 할 수 있습니다.

---

### step2

이제 step2 폴더로 이동해주세요.

```shell
cd ../step2
```

step2 에서는 git 명령어와 cut, head 를 실습할 예정입니다.

함수에 명령어를 작성하기 전

```shell
git stash list --pretty=format:$'%gd\t%cr\t%s'
```

명령어를 출력해보겠습니다.
Stash 목록이 나오는 걸 볼 수 있습니다.

그러면, 해당 명령어를 함수 내부에서 사용해보겠습니다.

```shell
echo-git-stash() {
  local stash_list
  stash_list=$(git stash list --pretty=format:$'%gd\t%cr\t%s')

  echo "stash 정보를 출력합니다"
  echo "$stash_list"

  # 아래에 작성
  # head 명령어를 통해 첫번째 라인을 추출한다
  local stash
  stash=$(echo "$stash_list" | head -n1)

  # cut 명령어를 통해 구분자를 기반으로 요소를 자른다. (%gd)
  local stash_id
  stash_id=$(echo "$stash" | cut -f1)

  # 첫번째 Stash ID 출력 및 stash 보기
  echo
  echo "가장 첫번째 Stash 의 ID 및 변경된 파일"
}
```

head 명령어는 여러 줄을 받으면, 지정된 수만큼만 출력해주는 역할을 합니다.
그리고, cut 명령어는 구분자를 기반으로 요소를 잘라주는 역할을 합니다.

```shell
git stash show -p "$stash_id" --color=always
```

이렇게 step2 가 끝났습니다.
우리가 터미널에서 그냥 실행하는 명령어들도
스크립트 내부에서 사용이 가능하다는 걸 알게되었습니다.

## step3

step3 에서는 fzf 를 사용해보고, fzf 에 있는 옵션들을 하나씩 확인합니다.

```shell
git stash list --pretty=format:$'%gd\t%cr\t%s'
```

```shell
git stash list --pretty=format:$'%gd\t%cr\t%s' | fzf
```

아까 git stash 목록을 보기위해 사용했던 stash list 명령어를 다시 입력해볼게요.
그리고, 이 뒤에 파이프라인 + fzf 를 입력합니다.

검색할 수 있는 fzf 화면이 제공됩니다.
엔터를 누르면, 선택된 라인이 출력되는걸 볼 수 있습니다.

이제, fzf 의 옵션을 하나씩 추가해볼건데요.
첫번째로, `layout="reverse"` 입니다.
현재는 검색하는 검색창이 밑에 있는데  이를 뒤집어서 위에서 검색할 수 있게 해줍니다.

두번째로, `header="Stash ID      |      생성 시간     |     메시지`
를 추가합니다.

기존에 목록 값들만 있어서, 목록의 요소들이 정확히 뭐를 의미하는지 헷갈릴수 있습니다.
이럴때 header 를 사용해 목록의 칼럼을 명시합니다.

세번째로, `preview` 옵션입니다.
preview 는 용어 그대로, 명령어에 대해 미리보기를 제공해줍니다.

`git stash show -p {1} --color=always` 명령어를

`--preview=git stash show -p {1} --color=always` preview 옵션에 추가하면
오른쪽에 git stash show -p 명령어가 실행되어 결과가 보여지는 것을 확인할 수 있습니다.

이제, 이 3가지 옵션을 활용하여 쉘 스크립트 함수를 작성해보겠습니다.

```shell
fzf-git-stash() {
  local stash_list
  stash_list=$(git stash list --pretty=format:$'%gd\t%cr\t%s')

  local selected_stash
  selected_stash=$(echo "$stash_list" \
      | fzf --layout="reverse" \
          --prompt="Select Stash > " \
          --header="Stash ID     |    Time Ago          | Message" \
          --preview="git stash show -p {1}")

  local stash_id
  stash_id=$(echo "$selected_stash" | cut -f1)

  echo
  echo "선택된 Stash 의 ID"
  echo "$stash_id"
}
```

prompt, header, preview 를 추가하고
source 로 함수를 불러오면?

함수를 실행하면, fzf 창이 표시가 된다.
그래서, 하나를 선택하면?
선택된 ID 가 출력이 되는걸 확인할 수 있습니다.

이제 마지막 step4 입니다!
저희가 지금까지 배운 요소들을 기반으로 stash 를 간편하게 사용할 수 있는 함수를 만듭니다.

함수를 vi 로 들어가서 차례대로 보면
step2 에서 진행했던, stash list 명령어를 통해 stash_list 를 가져왔고
stash_list 를 fzf 로 전달해줍니다.
preview 가 조금 길어졌는데, 단순히 더 깔끔하게 출력하기 위해 포맷을 맞춘것입니다.

그리고, 선택된 stash 에서
id 와 message 를 잘라서 출력해줍니다.

여기에서 저희는 apply, pop, drop 명령어를 추가하면 됩니다.

```shell
echo "(a)pply, (p)op, (d)rop "
read -r action
action=${action:0:1}
echo
case "$action" in
  a|A) git stash apply "$stash_id" ;;
  p|P) git stash pop "$stash_id" ;;
  d|D) git stash drop "$stash_id" ;;
  *)   echo "Cancelled." ;;
esac
```

action 이라는 명령어를 통해서?
a 이면, git stash apply 가 작동하게
p 이면, git stash pop 이 작동하게
d 이면, git stash drop 이 동작하게 작성합니다.

최종적으로는
우리가 입력한 명령어에 따라 stash 를 간편하게 관리할 수 있습니다.

실행해보면 기존과 같이 stash 목록이 뜨고
오른쪽에는 미리보기를 제공해주고 있습니다.

여기서 하나를 선택하면?
a, p, d 를 입력해달라고 뜨고
먼저, drop 을 해보겠습니다.

그러면 우리가 선택한 stash 가 사라진 것을 볼 수 있습니다.

두번째로 pop 을 해보면
pop 한 요소는 사라지고

git status 명령어를 보면
파일이 변경된 걸 감지한걸 볼 수 있습니다.

다시, git stash -m "다시 저장" 이라고 하면?

stash list 에 우리가 저장한 값이 보입니다.

이렇게 실습을 마무리 하겠습니다.
