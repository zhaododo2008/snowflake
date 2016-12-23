# snowflake
1.需要加入parent项目对应的依赖包

2.使用ab测试
ab -n 1000 -c 100 http://localhost:8080/snow-web/home/getSequence
