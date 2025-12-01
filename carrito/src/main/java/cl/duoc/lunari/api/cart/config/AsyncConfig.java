package cl.duoc.lunari.api.cart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono de tareas en segundo plano
 * Habilita la ejecución de métodos anotados con @Async
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Configura el executor de tareas asíncronas
     * Pool de hilos dedicado para jobs de post-procesamiento de pagos
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        logger.info("Configurando ThreadPoolTaskExecutor para tareas asíncronas");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configuración del pool de hilos
        executor.setCorePoolSize(2);           // Hilos mínimos activos
        executor.setMaxPoolSize(5);            // Hilos máximos
        executor.setQueueCapacity(100);        // Cola de tareas pendientes
        executor.setThreadNamePrefix("async-job-");

        // Política de rechazo cuando el pool está lleno
        executor.setRejectedExecutionHandler((r, e) -> {
            logger.warn("Tarea rechazada por el executor: Cola llena. Tarea: {}", r.toString());
        });

        // Comportamiento al shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        logger.info("ThreadPoolTaskExecutor configurado - CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * Executor alternativo para tareas de baja prioridad o procesamiento batch
     */
    @Bean(name = "batchTaskExecutor")
    public Executor batchTaskExecutor() {
        logger.info("Configurando ThreadPoolTaskExecutor para tareas batch");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("batch-job-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();

        return executor;
    }
}
