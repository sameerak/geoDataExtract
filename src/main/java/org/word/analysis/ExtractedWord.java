package org.word.analysis;

public class ExtractedWord implements Comparable<ExtractedWord> {
    private String term;

    private int count = 0;
    private long startTime, endTime;
    double frequency;

    public ExtractedWord(String term, Long startTime) {
        this.term = term;
        this.startTime = startTime;
        count = 1;
        frequency = 0;
    }

    public void incrementCounter(Long timestamp) {
        this.endTime = timestamp;
        ++count;
    }

    public void setFrequency() {
        double timeDiff = (endTime - startTime <= 0) ? 1 : (endTime - startTime);
        if (count == 1) {
            frequency = 0;
        } else if (count < 10 || timeDiff <= 86400) {
            this.frequency = count / 86400;
        } else {
            //calculate term generated per second
            this.frequency = (count * 1000) / timeDiff;
        }
    }

    public double getFrequency() {
        return frequency;
    }

    public int getCount() {
        return count;
    }

    public int compareTo(ExtractedWord extractedWord) {
        //sort descending order
        return frequency < extractedWord.getFrequency() ? 1 :
                frequency > extractedWord.getFrequency() ? -1 : 0;
    }

    public String getTerm() {
        return term;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
