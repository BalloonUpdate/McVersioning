package gui.extended

import cn.hutool.core.thread.ThreadUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.JProgressBar

/**
 * 平滑进度条
 */
class SmoothProgressBar(max: Int, private val flowTime: Int) : JProgressBar(0, max)
{
    //单线程线程池，用于保证进度条的操作顺序
    private val singleThreadExecutor: ExecutorService = ThreadPoolExecutor(
        1, 1,
        0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(10)
    )

    //每秒刷新频率
    private val frequency: Int = flowTime / 10

    /**
     * 以平滑方式设置进度
     *
     * @param value 新进度
     */
    override fun setValue(value: Int)
    {
        singleThreadExecutor.execute {
            val currentValue = getValue()
            if (value < currentValue) decrement(currentValue - value)
                else increment(value - currentValue)

        }
    }

    override fun setVisible(aFlag: Boolean)
    {
        //保证在进度条在完成先前所有的 加/减 操作后，再进行 setVisible 操作
        singleThreadExecutor.execute { super.setVisible(aFlag) }
    }

    /**
     * 重置进度条进度至 0, 不使用平滑进度
     *
     */
    fun reset()
    {
        super.setValue(0)
    }

    /**
     * 将进度条进度增长指定数值, 使用平滑方式
     *
     * @param value 增长的数值
     */
    fun increment(value: Int)
    {
        val currentValue = getValue()
        //如果变动的数值小于刷新速度，则使用变动数值作为刷新速度，否则使用默认刷新速度
        val finalFrequency = frequency.coerceAtMost(value)
        for (i in 1..finalFrequency)
        {
            val queueSize = (singleThreadExecutor as ThreadPoolExecutor).queue.size
            //防止任务过多
            if (queueSize > 5) break
            super.setValue(currentValue + value / finalFrequency * i)

            //如果线程池中的任务过多则加快进度条速度（即降低 sleep 时间）
            if (queueSize == 0) ThreadUtil.sleep((flowTime / (finalFrequency + i)).toLong())
                else ThreadUtil.sleep((flowTime / (finalFrequency + i) / queueSize).toLong())
        }

        //如果最后进度条的值差异过大，则重新进行一次 increment
        val lastValue = currentValue + value - getValue()
        if (lastValue >= 5 && (singleThreadExecutor as ThreadPoolExecutor).queue.size <= 1) increment(lastValue)

        //防止差异，设置为最终结果值
        super.setValue(currentValue + value)
    }

    /**
     * 将进度条进度减少指定数值, 使用平滑方式
     *
     * @param value 减少的数值
     */
    fun decrement(value: Int)
    {
        val currentValue = getValue()
        //如果变动的数值小于刷新速度，则使用变动数值作为刷新速度，否则使用默认刷新速度
        val finalFrequency = frequency.coerceAtMost(value)
        for (i in 1 until finalFrequency)
        {
            val queueSize = (singleThreadExecutor as ThreadPoolExecutor).queue.size
            //防止任务过多
            if (queueSize > 5) break
            super.setValue(currentValue - value / finalFrequency * i)

            //如果线程池中的任务过多则加快进度条速度（即降低 sleep 时间）
            if (queueSize == 0) ThreadUtil.sleep((flowTime / (finalFrequency + i)).toLong())
                else ThreadUtil.sleep((flowTime / (finalFrequency + i) / queueSize).toLong())
        }

        //如果最后进度条的值差异过大，则重新进行一次 increment
        val lastValue = currentValue - value - getValue()
        if (lastValue >= 5 && (singleThreadExecutor as ThreadPoolExecutor).queue.size <= 1) decrement(lastValue)

        //防止差异，设置为最终结果值
        super.setValue(currentValue - value)
    }
}