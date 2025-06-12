import csv
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from collections import defaultdict

EVENT_COLORS = {
    'STARTED': 'tab:green',
    'WAITING_IO': 'tab:orange',
    'WAITING_Memoria': 'tab:purple',
    'WAITING_Processo_Filho': 'tab:gray',
    'WAIT_FINISHED': 'tab:blue',
    'FINISHED': 'tab:red',
    'ALL_FINISHED': 'black'
}

def parse_log(filename):
    events = []
    with open(filename, newline='') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            if len(row) != 3:
                continue
            process, time, event = row
            if process == 'todos':
                continue
            events.append((process, int(time), event))
    return events

def build_timeline(events):
    timeline = defaultdict(list)
    for process, time, event in events:
        timeline[process].append((time, event))
    return timeline

def plot_timeline(timeline):
    fig, ax = plt.subplots(figsize=(10, 6))
    yticks = []
    yticklabels = []
    legend_handles = {}

    for i, (proc, evts) in enumerate(sorted(timeline.items())):
        evts = sorted(evts, key=lambda x: x[0])
        prev_time = None
        prev_event = None
        for time, event in evts:
            if prev_time is not None and prev_event is not None:
                color = EVENT_COLORS.get(prev_event, 'tab:gray')
                ax.broken_barh([(prev_time, time - prev_time)], (i - 0.4, 0.8), facecolors=color)
                if prev_event not in legend_handles:
                    legend_handles[prev_event] = mpatches.Patch(color=color, label=prev_event)
            prev_time = time
            prev_event = event
        # Mark the last event as a dot
        if prev_time is not None and prev_event is not None:
            color = EVENT_COLORS.get(prev_event, 'tab:gray')
            ax.plot(prev_time, i, 'o', color=color)
            if prev_event not in legend_handles:
                legend_handles[prev_event] = mpatches.Patch(color=color, label=prev_event)
        yticks.append(i)
        yticklabels.append(proc)

    ax.set_yticks(yticks)
    ax.set_yticklabels(yticklabels)
    ax.set_xlabel('Cycle')
    ax.set_title('Process Execution Timeline')
    ax.legend(handles=list(legend_handles.values()), loc='upper right')
    plt.tight_layout()
    plt.show()

if __name__ == '__main__':
    events = parse_log('./process_log.csv')
    timeline = build_timeline(events)
    plot_timeline(timeline)