# 최소 신장 트리와 최단 경로 알고리즘

> 강의 링크 : https://www.youtube.com/watch?v=QOWgP2_kGYQ

그래프는 정점과 간선의 집합

- 정점 : 스스로 존재할 수 있는 객체
- 간선 : 정점을 잇는 객체
- 가중치 : 간선을 값을 가진다면 그 값

## 최소 신장 트리

최소 비용으로 그래프 모든 정점을 연결하는 트리

### 크루스칼 알고리즘

1. 그래프의 간선들 중 가장 낮은 가중치 간선 선택
2. 해당 간선이 사이클을 만들지 않는다면 트리에 추가
3. 모든 간선들에 대해 반복

```java
public Tree kruskal(List<Edge> graph){
    Tree tree = new Tree();
    graph = sortByAsc(graph);
    
    for(Edge edge: graph){
        if(tree.detectCycle(edge)){
            continue;
        }
        tree.add(edge);
    }
    return tree;
} 
```
### 프림 알고리즘

1. 시작 정점 정하고, 트리 집합에 포함
2. 트리와 연결된 간선들 중 최소 가중치인 간선을 트리 집합에 포함
3. 트리 집합이 N-1 개의 간선을 가질 때까지 반복한다

```java
public Tree prim(Node start,List<Edge> graph,int n){
    Tree tree = new Tree();
    tree.setStart(start);
    
    // 간선 n-1 추가할 때 까지 반복
    for(int i =0;i<n-1;i++){
        // 트리와 인접한 정점을 연결한 간선 중 가중치 낮은 간선 찾음
        Edge edge = findSmallestEdge(tree, graph);
        tree.add(edge);
    }
    return tree;
}
```

> 결국, 최소 간선들을 계속 갱신시키는 것

## 최단 경로 알고리즘

가중 그래프에서 두 정점을 가장 적은 비용으로 연결하는 간선들을 찾는 문제

### 다익스트라 알고리즘

- cost : 시작 정점부터 현재 정점까지 비용
- visit : 방문해야 할 정점 저장

1. 시작 정점에서 다른 정점까지 비용을 충분히 큰 값으로 저장 
2. 시작 정점을 visit 에 추가
3. visit 에서 가장 비용이 정점을 꺼내 현재 정점으로 설정
4. 현재 정점과 인접한 정점들에 도달하는 비용을 기존 비용과 비교해 최소값으로 갱신, 해당 인접 정점들을 visit 에 추가
5. visit 이 빌 때까지 (3) ~ (4) 를 반복한다

### A* 알고리즘

f(x) = g(x) + h(x)

- f(x) : cost
- g(x) : 정점 x까지의 비용
- h(x) : 도착 정점까지 휴리스틱 함수

#### 휴리스틱 함수

유클리디안 거리 ( 직선 거리 )
맨하탄 거리 ( 직각 거리 )

스타크래프트, 배민 배차 시스템에서 이용 되었다고 함

요샌, CH 이나 CCH 알고리즘을 쓴다고 함
