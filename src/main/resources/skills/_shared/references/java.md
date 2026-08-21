# Java core

## Collection
- HashMap: bang bam, xu ly va cham, co che mo rong (resize), load factor 0.75
- ConcurrentHashMap: khac biet giua Java 7 (segment) va Java 8 (CAS + synchronized)
- ArrayList vs LinkedList: chi phi truy cap ngau nhien, chen giua, tieu ton bo nho
- fail-fast va ConcurrentModificationException: nguyen nhan va cach tranh

## Da luong
- Vong doi Thread, khac nhau giua start() va run()
- synchronized vs ReentrantLock: tinh nang, kha nang ngat, fairness
- volatile: dam bao visibility nhung khong dam bao atomicity
- ThreadPoolExecutor: 7 tham so, thu tu xu ly khi hang doi day, cac policy tu choi
- Virtual thread trong Java 21+: khac gi platform thread, khi nao dung hop ly

## JVM
- Phan vung bo nho: heap, stack, metaspace, program counter
- Co che nhan dien rac: reachability analysis, cac loai GC root
- Class loading: cac giai doan, co che parent delegation
- Cac cong cu chan doan: jstack, jmap, jstat dung khi nao
