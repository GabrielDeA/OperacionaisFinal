import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

EVENT_COLORS = {
    'STARTED': 'tab:green',
    'WAITING_IO': 'tab:orange',
    'WAITING_Memoria': 'tab:purple',
    'WAITING_Processo_Filho': 'tab:gray',
    'WAIT_FINISHED': 'tab:blue',
    'FINISHED': 'tab:red',
    'ALL_FINISHED': 'black'
}

def parse_log(log_path):
    timeline = {}
    with open(log_path) as f:
        for line in f:
            line = line.strip()
            if not line or line.count(',') != 2:
                continue
            proc, time, event = line.split(',')
            time = int(time)
            timeline.setdefault(proc, []).append((time, event))
    return timeline
def plot_timeline(timeline):
    import matplotlib.pyplot as plt
    import matplotlib.patches as mpatches

    max_time = max(time for evts in timeline.values() for time, _ in evts) + 1

    fig, ax = plt.subplots(figsize=(10, 6))
    yticks, yticklabels, legend_handles = [], [], {}
    bar_height = 0.8
    short_width = 0.1

    for i, (proc, evts) in enumerate(sorted(timeline.items())):
        evts = sorted(evts, key=lambda x: (x[0], x[1]))
        t = 0
        for idx, (time, event) in enumerate(evts):
            # If multiple events at the same timestamp, show each as a short bar
            if idx + 1 < len(evts) and evts[idx + 1][0] == time:
                width = short_width
            else:
                # Next event is at a different timestamp
                next_time = evts[idx + 1][0] if idx + 1 < len(evts) else max_time
                width = max(next_time - time, short_width)
            color = EVENT_COLORS.get(event, 'tab:gray')
            ax.broken_barh([(time, width)], (i - bar_height / 2, bar_height), facecolors=color)
            if event not in legend_handles:
                legend_handles[event] = mpatches.Patch(color=color, label=event)
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
    timeline = parse_log('../process_log.csv')
    plot_timeline(timeline)