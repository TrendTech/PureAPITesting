# PureAPITesting
接口测试中有一种纯接口测试，类似于单元测试，是无上下文依赖性的测试，和传统的有依赖性的业务流程接口测试比起来单接口的测试范围更广，基本上可以做到全接口的覆盖，主要验证接口的侧重点也偏重于通用接口的规范，基本的业务以及接口可能出现的错误漏洞等等。

此工具基本思路是解析现有swagger的api并自动化生成针对每个接口的测试信息以及模拟的请求数据，将这些组合成测试用例，这些用例数据是以csv文件保存的，最终在Jmeter中执行，同时这个工具还可以以非常方便快捷的方式让测试人员写接口的测试用例，实际使用起来远快过postman。