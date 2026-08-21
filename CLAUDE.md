# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Dự án

Nền tảng phỏng vấn bằng AI (dự án học). Backend Spring Boot + Spring AI + Gemini + pgvector.
Người làm là người mới — **giải thích bằng tiếng Việt**, ưu tiên code rõ ràng hơn code "thông minh",
và làm theo bậc thang: mỗi bước phải chạy được rồi mới sang bước sau.

Tham khảo kiến trúc: https://github.com/Snailclimb/interview-guide (học cấu trúc, không copy nguyên xi).

## Lệnh thường dùng

```powershell
.\gradlew compileJava        # kiểm tra nhanh, dùng thay cho việc tin vào lỗi đỏ của IDE
.\gradlew bootRun            # chạy app ở cổng 8080
.\gradlew test               # boot toàn bộ context qua AiInterviewApplicationTests.contextLoads
.\gradlew test --tests "AiInterviewApplicationTests"
```

Database chạy bằng Docker, **cổng 5433** (5432 đã bị dự án khác chiếm):

```powershell
docker exec -it ai-interview-db psql -U hp -d appdb -c "\dt"
```

**Gradle là nguồn sự thật.** IDE hay báo lỗi import sai sau khi thêm dependency — nếu
`.\gradlew compileJava` pass mà IDE vẫn đỏ thì đó là cache IDE lệch, chạy
`Java: Reload Projects` trong VS Code.

## Cấu hình đã debug — đừng đổi nếu không có lý do

Các giá trị dưới đây từng làm app chết và đã mất công tìm ra. Nếu cần đổi, phải nói rõ lý do.

- **Model chat `gemini-3.6-flash`** — `gemini-2.5-flash` và `gemini-2.0-flash` đã bị Google gỡ,
  trả về 404 kèm message chỉ luôn model thay thế. Khi gặp 404, đọc kỹ message đó.
- **Model embedding `gemini-embedding-001` + `dimensions: 768`** — `text-embedding-004` đã bị gỡ.
  Model mới mặc định ra **3072 chiều**; thiếu dòng `dimensions: 768` là lệch với `vector(768)` của
  pgvector và vỡ lúc insert.
- **API key khai hai chỗ**: `spring.ai.google.genai.api-key` (chat) **và**
  `spring.ai.google.genai.embedding.api-key` (embedding). Embedding không kế thừa key của chat;
  thiếu nó Spring AI rơi sang nhánh Vertex AI và ném `"Google GenAI project-id must be set!"`.
- **Không đặt `project-id` hay `location`** ở bất kỳ đâu — có là client chuyển sang Vertex AI và
  từ chối API key kiểu AI Studio.
- **`spring-ai-google-genai-embedding`** phải khai tay trong `build.gradle`; starter chat không
  kéo nó theo (khai `optional`).
- **`spring-ai-rag`** cũng phải khai tay. `QuestionAnswerAdvisor` **không tồn tại** ở Spring AI
  2.0.0 — dùng `RetrievalAugmentationAdvisor`.

### Spring Boot 4 dùng Jackson 3

`ObjectMapper` phải import từ **`tools.jackson.databind`**, không phải `com.fasterxml.jackson.databind`.
Bản `com.fasterxml` vẫn nằm trên classpath (thư viện khác kéo về) nên IDE hay gợi ý nhầm, nhưng
Spring chỉ đăng ký bean cho Jackson 3. Import sai thì app không khởi động nổi với thông báo
`required a bean of type 'com.fasterxml.jackson.databind.ObjectMapper' that could not be found`.

## Kiến trúc

Modular monolith theo DDD. Mỗi module dưới `modules/` tự chứa đủ tầng và **không import lẫn nhau**.

```
com/hp/ai_interview/
├── common/                 result/ (Result<T> thống nhất), exception/ (GlobalExceptionHandler)
└── modules/
    ├── chat/               hỏi đáp tự do, có trí nhớ hội thoại
    ├── knowledgebase/      RAG: nạp tài liệu, hỏi đáp có dẫn nguồn
    └── interview/          phỏng vấn thử theo Skill, chấm điểm
```

Trong mỗi module: `controller/` → `service/` → `repository/`, còn `model/` chia
**`model/dto/`** (kiểu vào ra API) và **`model/entity/`** (bảng JPA).

Mọi API trả về `Result<T>` với hình dạng `{code, message, data}` — `code: 0` là thành công.
Lỗi được `GlobalExceptionHandler` hứng: `MethodArgumentNotValidException` và
`IllegalArgumentException` → 400, còn lại → 500 kèm log nguyên exception nhưng response chỉ trả
câu chung chung.

### Hai cơ chế lấy kiến thức khác nhau — đừng nhầm

Đây là điểm dễ hiểu sai nhất của dự án.

| | `knowledgebase` (RAG) | `interview` (Skill) |
| --- | --- | --- |
| Nguồn | tài liệu người dùng upload | file `.md` viết tay trong `resources/skills/` |
| Lưu ở | pgvector 768 chiều | classpath |
| Cách chọn | embedding + cosine similarity | `switch` theo `priority` trong `skill.meta.yml` |
| Tốn API để chọn | có | **không** |

RAG chỉ dùng khi nội dung **không biết trước**. Câu hỏi phỏng vấn Java là tập biết trước nên
`interview` đọc thẳng file dàn ý rồi nối vào prompt — `SkillService.buildReferenceSection()`
chỉ là đọc file và ghép chuỗi, cắt ở 12000 ký tự.

### Module `interview`

Bộ đề được **sinh cả loạt một lần** lúc tạo phiên (không hỏi tới đâu sinh tới đó), lưu JSON vào
cột `questions_json`. Chấm điểm cũng chấm **cả phiên một lần** lúc `complete`.

`resources/skills/<id>/skill.meta.yml` khai các category kèm `priority`, quyết định thuật toán
chia đề trong `SkillService.calculateAllocation()`:
`ALWAYS_ONE` giữ đúng 1 câu → mọi category được 1 câu → phần dư chia vòng tròn cho `CORE`.

`SKILL.md` là persona, nối thẳng vào system prompt.

Thêm hướng phỏng vấn mới = tạo thêm thư mục trong `resources/skills/`, **không sửa code**.

### Prompt để ngoài code

Toàn bộ prompt của module `interview` nằm ở `resources/prompts/*.st`, nạp bằng `PromptTemplate`
lúc khởi tạo service. Sửa cách ra đề hoặc cách chấm điểm không cần compile lại.

### Structured output

Dùng `.entity(SomeRecord.class)` của `ChatClient` để ép model trả JSON đúng schema (record lồng
record vẫn chạy). Nhưng **luôn kiểm lại ở phía Java**: model có thể trả thừa, thiếu, hoặc bỏ sót
phần tử dù prompt đã ràng buộc. Xem `InterviewQuestionService.convert()` (cắt đúng số câu, có
6 câu dự phòng khi model lỗi) và `InterviewSessionService.applyEvaluation()` (ghép theo
`questionIndex`, thiếu thì log rồi bỏ qua).

## Migration

Flyway, `ddl-auto: validate` — schema đổi thì **phải viết migration**, Hibernate không tự tạo bảng.
Bảng `vector_store` là ngoại lệ: Spring AI tự tạo qua `initialize-schema: true`, đồng thời tự chạy
`CREATE EXTENSION` cho `vector`, `hstore`, `uuid-ossp` (nên user DB cần quyền superuser).

## Bí mật

API key và mật khẩu DB nằm trong `src/main/resources/application-local.yml` (profile `local`),
file này đã được gitignore cùng với `guide/`. Không hardcode key vào `application.yaml` chính
hay vào code. Thư mục `.claude/` KHÔNG ignore — nó chứa các skill UI/UX dùng cho frontend.

## Ghi chú vận hành

- Tạo phiên phỏng vấn mất **~25 giây**, chấm điểm **~20 giây** — cả hai đều đồng bộ. Đây là chỗ
  sẽ cần chuyển sang chạy nền khi số câu hỏi tăng.
- Sau khi chạy thử có tạo dữ liệu, nhớ dọn `interview_session`, `interview_answer`,
  `knowledge_document`, `vector_store`.
- Thư mục `guide/` chứa hướng dẫn từng phase kèm kết quả test thật — đọc ở đó để hiểu vì sao một
  quyết định được đưa ra.

## Frontend aesthetics

Ngôn ngữ thiết kế mô phỏng **Coderbyte** (coderbyte.com — user đã chốt bằng screenshot),
nền sáng B2B chuyên nghiệp. Giữ nhất quán, đừng đổi từng màn một kiểu.

- **Chữ ký Coderbyte**: banner gradient teal→blue→tím trên cùng (`.brand-gradient`), nav chữ hoa
  giãn cách `tracking-[0.15em]`, nút CTA **pill navy** (`rounded-full bg-ink`), headline khổng lồ
  với vài chữ gradient (`.text-gradient`), section feature xen kẽ trái/phải, số liệu gradient,
  hàng topic chip chạy marquee.
- **Typography**: heading **Plus Jakarta Sans** (extrabold), body **DM Sans**. KHÔNG dùng
  Inter/Roboto/Arial/font hệ thống. Chữ câu hỏi phỏng vấn cỡ lớn (20px+).
- **Màu**: ink `#0b1f33` (navy, nút + heading), accent `#3b82f6`, violet `#7c3aed` (link phụ),
  gradient `#2dd4bf → #3b82f6 → #7c3aed`. Nền trắng, section xen kẽ `slate-50`. Có dark mode
  (class `dark` trên `<html>`, key localStorage `theme-v2`); nút navy đổi thành `dark:bg-accent`.
- **Chuyển động**: Framer Motion — `Reveal`/`RevealGroup` trong `components/motion.tsx`
  (fade-up whileInView, stagger), thanh điểm animate width, marquee chip. 200-500ms, ease-out.
- **Stack**: React + TypeScript + Vite + Tailwind CSS v4. Semantic HTML + ARIA.
- Frontend nằm ở `frontend/`, gọi backend qua proxy `/api` (vite.config), KHÔNG hardcode
  `http://localhost:8080` trong code gọi API.
- Tầng `src/api/`: một file mỗi module backend, mọi response bọc trong `Result<T>`
  `{code, message, data}` — `code !== 0` thì ném lỗi với `message`.
