package ru.geekbrains.march.chat.server;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class ServerApp {

    public static void main(String[] args) throws Exception {
        new Server(8189);

    }
}
// КОММЕНТАРИИ ПО ПУЛУ ПОТОКОВ:
// На сервере создала пул потоков, и теперь, насколько понимаю, при запуске каждого клиента не тратится
// время на создание своего отдельного потока, а запускается поток из пула. Это должно, по идее, давать
// экономию времени в случае, если одномоментно будут именно подключаться много клиентов. А в процессе уже
// общения, выходит, никакой разницы нет, ведь у нас и в базовой реализации для каждого ClientHandler был
// свой отдельный поток, в котором он отправлял сообщения. Т.е., по большому счету, вариант с пулом
// не особо тянет на оптимизацию,т.к. вероятность именно одномоментной регитрации кучи клиентов не так велика.
// Я реализовала с CachedThreadPool, потому что с нашей базой данных, конечно,  можно бы было сделать
// более "безопасный" FixedThreadPool с потолком в 3 потока, но ведь в теории чат он должен расти. Поэтому
// из вариантов пулов подходит, наверное, все-таки CachedThreadPool, который не очень рекомендован к
// использованию. Наверное, мы дальше пройдем этот вопрос, но на практике в таком случае, раз CachedThreadPool
// условно-опасен без потолка, как поступают? - применяют только если на 100% увереены, что не будет никогда
// неподъемной пиковой нагрузки? или знают, какую нагрузку может потянуть машина и используют FixedThreadPool
// с этим максимальным потолком?

// У меня в коде в Server  небольшой технческий вопрос по строкам 36 и 52 есть (там обозначила, но дублирую):
// а зачем в данном случае использовать ключевое слово  this? это как-то важно? (я просто вижу, что там же,
// вроде, можно и просто имя переменной указать)

