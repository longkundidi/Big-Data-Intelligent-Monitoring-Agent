export function pieOption () {
  this.title = {
    text: '故障诊断结果',
    left: 'center'
  }
  this.tooltip = {
    trigger: 'item'
  }
  this.legend = {
    orient: 'vertical',
      left: 'left'
  }
  this.series = [
    {
      name: 'Access From',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 1048, name: '部件1' },
        { value: 548, name: '部件2' },
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]

}
