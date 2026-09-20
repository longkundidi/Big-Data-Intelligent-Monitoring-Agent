
//  多线堆叠(StackedLine)
export function multLineOption (titleText, legendData, yAxisName) {
  const series = [];
  const markArea = {
    silent: true, // 不显示提示框
    label: {
      show: false
    },
    itemStyle: {
      color: 'rgba(255, 173, 177, 0.4)'
    },
    data: []
  };

  legendData.forEach((element, index) => {
    series.push({
      name: element,
      data: [],
      type: 'line',  //图的类型
      symbol: 'none',
      itemStyle: {
        normal: {
          lineStyle: {
            width: 2,
            type: index === 1 ? 'solid' : 'solid'
          }
        }
      },
      markArea: markArea
    });
  });

  return {
    title: {
      text: titleText,
      textStyle: {
        fontSize: 14,
        fontWeight: 'bolder',
        color: '#e8fbff'
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      top: "5%",
      left: '0%',
      data: legendData,
      textStyle: {
        fontSize: 12,
        color: 'rgba(232,251,255,0.78)'
      }
    },
    grid: {
      top: '18%',
      left: '4%',
      right: '5%',
      bottom: '20%',
      containLabel: false,
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
    },
    xAxis: {
      name: '时间',
      nameLocation: 'end',
      data: [],
      axisLine: {
        show: true,
        lineStyle: {
          color: 'rgba(141,231,207,0.35)'
        }
      },
      axisLabel: {
        color: 'rgba(232,251,255,0.7)'
      },
      nameTextStyle: {
        color: 'rgba(232,251,255,0.7)'
      },
      splitLine: {
        show: false
      }
    },
    yAxis: {
      name: yAxisName,
      type: 'value',
      axisLine: {
        show: true,
        lineStyle: {
          color: 'rgba(141,231,207,0.35)'
        }
      },
      axisLabel: {
        interval: 800,
        color: 'rgba(232,251,255,0.7)'
      },
      nameTextStyle: {
        color: 'rgba(232,251,255,0.7)'
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: ['rgba(65,228,187,0.1)']
        }
      }
    },
    series: series,
    color: ['#0780f9', '#FFA438', '#FD0606'] // 定义折线颜色
  };
}

export function multLineOptionNoTitle (legendData) {
  const series = [];
  const markArea = {
    silent: true, // 不显示提示框
    label: {
      show: false
    },
    itemStyle: {
      color: 'rgba(255, 173, 177, 0.4)'
    },
    data: []
  };

  legendData.forEach((element, index) => {
    series.push({
      name: element,
      data: [],
      type: 'line',  //图的类型
      symbol: 'none',
      itemStyle: {
        normal: {
          lineStyle: {
            width: 2,
            type: index === 1 ? 'solid' : 'solid'
          }
        }
      },
      markArea: markArea
    });
  });

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      top: "5%",
      left: '0%',
      data: legendData,
      textStyle: {
        fontSize: 12,
        color: 'rgba(232,251,255,0.78)'
      }
    },
    grid: {
      top: '18%',
      left: '4%',
      right: '5%',
      bottom: '10%',
      containLabel: false,
    },
    xAxis: {
      data: [],
      axisLine: {
        show: true,
        lineStyle: {
          color: 'rgba(141,231,207,0.35)'
        }
      },
      axisLabel: {
        color: 'rgba(232,251,255,0.7)'
      },
      splitLine: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: true,
        lineStyle: {
          color: 'rgba(141,231,207,0.35)'
        }
      },
      axisLabel: {
        interval: 800,
        color: 'rgba(232,251,255,0.7)'
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: ['rgba(65,228,187,0.1)']
        }
      }
    },
    series: series,
    color: ['#0780f9', '#FD0606'] // 定义折线颜色
  };
}
