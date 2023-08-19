# healthcheck

HealthCheckController 설정
---
> net.mobon.healthcheck.api.controller.HealthCheckController

 - api 호출하는 방법
 - fixedDelay 마다 동작
   - fixedDelay = 60000 은 1분을 의미합니다.

HealthCheckService 설정
---
> net.mobon.healthcheck.api.service.HealthCheckService

 - list.add("127.0.0.1"); 등을 이용하여, check 할 서버의 url 을 입력합니다.
   - 해당 서버에서는 해당 api 로 return 할 api 를 만들어둬야합니다.
   
Telegram 의 url 설정
---
> application.properties

 - telegram.url=https://api.telegram.org/  
Telegram으로 HealthCechk 결과 송출하므로 본인이 만든 url을 등록해야합니다.

Spring boot 사용
---
> SwaggerConfig 
를 사용해서 상태값을 확인할 수 있습니다.


