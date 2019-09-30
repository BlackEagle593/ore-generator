package de.eaglefamily.oregenerator.util;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.disposables.Disposables;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class BukkitRxWorker extends Scheduler.Worker {

  private final Plugin plugin;
  private final Scheduler scheduler;
  private final BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  /**
   * Creates an instance of bukkit rx worker.
   *
   * @param plugin the plugin
   */
  public BukkitRxWorker(final Plugin plugin) {
    this.plugin = plugin;
    scheduler = new Scheduler() {
      @Override
      public Worker createWorker() {
        return new BukkitRxWorker(plugin);
      }
    };
  }

  @Override
  public synchronized Disposable schedule(final Runnable run, final long delay,
                                          final TimeUnit unit) {
    if (compositeDisposable.isDisposed()) {
      return Disposables.disposed();
    }

    final int taskId =
        bukkitScheduler
            .scheduleSyncDelayedTask(plugin, run, Math.round(unit.toMillis(delay) * 0.02));
    if (taskId < 0) {
      return Disposables.disposed();
    }

    final Disposable disposable = new Disposable() {
      @Override
      public void dispose() {
        bukkitScheduler.cancelTask(taskId);
      }

      @Override
      public boolean isDisposed() {
        return !(bukkitScheduler.isQueued(taskId) || bukkitScheduler.isCurrentlyRunning(taskId));
      }
    };

    compositeDisposable.add(disposable);
    return disposable;
  }

  @Override
  public synchronized void dispose() {
    compositeDisposable.dispose();
  }

  @Override
  public synchronized boolean isDisposed() {
    return compositeDisposable.isDisposed();
  }

  /**
   * Gets the bukkit rx scheduler.
   *
   * @return the scheduler
   */
  public Scheduler getScheduler() {
    return scheduler;
  }
}
