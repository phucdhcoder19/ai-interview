# Spring / Spring Boot

## IoC va Bean
- IoC giai quyet van de gi: tach roi viec tao doi tuong khoi viec dung doi tuong
- @Component vs @Bean: cach khai bao, tich hop thu vien ben thu ba
- Constructor injection vs setter vs field: tinh bat bien, phu thuoc vong, kha nang test
- Bean scope: singleton, prototype, request, session; phan tich an toan luong
- Vong doi bean: khoi tao -> gan thuoc tinh -> Aware -> init -> huy

## AOP
- Khai niem: aspect, pointcut, advice, join point
- Spring AOP vs AspectJ: proxy dong luc chay vs det ma luc bien dich
- Vi sao goi noi bo trong cung class lam AOP mat tac dung, cach xu ly

## Transaction
- @Transactional: propagation, isolation, rollbackFor
- Bay truong hop propagation, dac biet REQUIRED / REQUIRES_NEW / NESTED
- Cac tinh huong transaction mat tac dung: goi noi bo, method khong public, nuot exception

## Spring Boot
- Auto configuration hoat dong the nao, vai tro cua @ConditionalOnMissingBean
- Starter la gi, vi sao khong can khai bao version
- Cach quan ly cau hinh theo profile
