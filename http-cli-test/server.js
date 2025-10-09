const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const app = express();
const port = 3000;

// POST 요청의 JSON 본문을 파싱하기 위한 미들웨어
app.use(express.json());

// --- 파일 업로드 설정 ---
const uploadDir = 'uploads';
// uploads 디렉토리가 없으면 생성
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir);
}
const upload = multer({ dest: 'uploads/' });


// 1. 기본 GET 요청을 위한 루트 엔드포인트
app.get('/', (req, res) => {
  res.send('Hello, httpie! This is the root endpoint.\n');
});

// 2. JSON 응답을 위한 엔드포인트
app.get('/data', (req, res) => {
  res.json({
    message: "This is a JSON response",
    user: {
      id: 123,
      name: "Alice"
    },
    items: ["item1", "item2", "item3"]
  });
});

// 3. POST 요청으로 받은 JSON 데이터를 그대로 돌려주는 'echo' 엔드포인트
app.post('/echo', (req, res) => {
  console.log('Received data:', req.body);
  res.json({
    message: "You sent this data:",
    receivedData: req.body
  });
});

// 4. 리다이렉트를 테스트하기 위한 엔드포인트
app.get('/redirect', (req, res) => {
  console.log('Redirecting to /data');
  res.redirect('/data');
});

// 5. 기본 인증(Basic Authentication)을 테스트하기 위한 엔드포인트
app.get('/private', (req, res) => {
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    res.status(401).send('Authentication required\n');
    return;
  }

  // 'Basic base64encoded_credentials' 형식
  const [type, credentials] = authHeader.split(' ');
  const [username, password] = Buffer.from(credentials, 'base64').toString().split(':');

  console.log(type);
  console.log(username, password);

  if (type === 'Basic' && username === 'admin' && password === 'password123') {
    res.send('Welcome to the private area!\n');
  } else {
    res.status(403).send('Invalid credentials\n');
  }
});

// 6. 파일 업로드를 처리하는 엔드포인트
app.post('/upload', upload.single('myFile'), (req, res) => {
  if (!req.file) {
    return res.status(400).send('No file uploaded.\n');
  }
  console.log('File uploaded:', req.file);
  res.send(`File '${req.file.originalname}' uploaded successfully to server path: ${req.file.path}\n`);
});


// 서버 시작
app.listen(port, () => {
  console.log(`Test server listening on http://localhost:${port}`);
});