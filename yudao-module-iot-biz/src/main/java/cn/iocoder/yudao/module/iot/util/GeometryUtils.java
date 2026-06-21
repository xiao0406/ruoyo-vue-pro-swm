package cn.iocoder.yudao.module.iot.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 几何计算工具类
 * 
 * 功能特性：
 * - 射线法判断点是否在多边形内部
 * - 智能多边形顶点排序（支持凸多边形和凹多边形）
 * - 自动算法选择机制（性能与兼容性平衡）
 * - 调试输出支持
 * 
 * 算法支持：
 * 1. 角度排序法：适用于凸多边形，O(n log n)时间复杂度，性能优秀
 * 2. 最近邻边界追踪法：适用于凹多边形，O(n²)时间复杂度，兼容性好
 * 3. 智能选择：根据多边形特征自动选择最适合的算法
 * 
 * 使用场景：
 * - 围栏范围检测（矩形、三角形、多边形围栏）
 * - 区域边界判断
 * - 几何形状处理
 * 
 * @author Shawn
 * @date 2025/07/09
 */
public class GeometryUtils {

    /**
     * 判断一个点是否在多边形内部
     * 使用射线法(Ray Casting Algorithm)实现
     * 
     * @param x       待判断点的X坐标
     * @param y       待判断点的Y坐标
     * @param polygon 多边形顶点坐标数组，格式为 [[x1,y1], [x2,y2], ..., [xn,yn]]
     * @return 如果点在多边形内部返回true，否则返回false
     */
    public static boolean isPointInPolygon(double x, double y, double[][] polygon) {
        if (polygon == null || polygon.length < 3) {
            return false; // 多边形至少需要3个点
        }

        boolean inside = false;
        int n = polygon.length;

        // 遍历多边形的每条边
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygon[i][0], yi = polygon[i][1];
            double xj = polygon[j][0], yj = polygon[j][1];

            // 判断点是否在多边形的某条边上
            if (((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }

            // 检查点是否在当前边上
            if (isPointOnLine(x, y, xi, yi, xj, yj)) {
                return true;
            }
        }

        return inside;
    }

    /**
     * 判断一个点是否在多边形内部（接收List参数版本，自动排序顶点）
     * 
     * @param x             待判断点的X坐标
     * @param y             待判断点的Y坐标
     * @param polygonPoints 多边形顶点坐标列表，每个点是一个包含[x,y]的数组
     * @return 如果点在多边形内部返回true，否则返回false
     */
    public static boolean isPointInPolygon(double x, double y, List<double[]> polygonPoints) {
        if (polygonPoints == null || polygonPoints.size() < 3) {
            return false;
        }

        // 对顶点进行排序，确保按照正确的几何顺序
        List<double[]> sortedPoints = sortPolygonVertices(polygonPoints);

        // 输出调试信息（注意：生产环境可能需要调整日志级别）
//        System.out.println("使用算法：智能顶点排序（自动选择最适合的算法）");
//        System.out.println("顶点排序前: " + formatPointsForDebug(polygonPoints));
//        System.out.println("顶点排序后: " + formatPointsForDebug(sortedPoints));

        double[][] polygon = new double[sortedPoints.size()][2];
        for (int i = 0; i < sortedPoints.size(); i++) {
            polygon[i] = sortedPoints.get(i);
        }

        return isPointInPolygon(x, y, polygon);
    }

    /**
     * 强制使用指定算法排序多边形顶点
     * 用于测试或特殊场景
     * 
     * @param polygonPoints 多边形顶点坐标列表
     * @param useAngleSort  是否使用角度排序法，false表示使用边界追踪法
     * @return 排序后的顶点列表
     * @author Shawn
     * @date 2025/07/09
     */
    public static List<double[]> sortPolygonVerticesWithAlgorithm(List<double[]> polygonPoints, boolean useAngleSort) {
        if (polygonPoints == null || polygonPoints.size() < 3) {
            return new ArrayList<>(polygonPoints != null ? polygonPoints : new ArrayList<>());
        }

        if (useAngleSort) {
            System.out.println("强制使用：角度排序算法");
            return sortVerticesByAngle(polygonPoints);
        } else {
            System.out.println("强制使用：最近邻边界追踪算法");
            return nearestNeighborBoundaryTracing(polygonPoints);
        }
    }

    /**
     * 对多边形顶点按照逆时针方向排序
     * 智能选择算法：优先使用快速的角度排序法，对于复杂情况使用最近邻追踪法
     * 同时支持凸多边形和凹多边形
     * 
     * @param polygonPoints 多边形顶点坐标列表
     * @return 排序后的顶点列表
     * @author Shawn
     * @date 2025/07/09
     */
    public static List<double[]> sortPolygonVertices(List<double[]> polygonPoints) {
        if (polygonPoints == null || polygonPoints.size() < 3) {
            return new ArrayList<>(polygonPoints != null ? polygonPoints : new ArrayList<>());
        }

        // 对于简单情况（3-4个点），优先尝试角度排序算法
        if (polygonPoints.size() <= 4) {
            return sortSimplePolygonVertices(polygonPoints);
        }

        // 对于复杂多边形，智能选择算法
        boolean isLikelyConvex = isLikelyConvexPolygon(polygonPoints);
        if (isLikelyConvex) {
            // 可能是凸多边形，使用快速的角度排序法
            System.out.println("多边形分析：检测为可能的凸多边形，使用角度排序算法");
            return sortVerticesByAngle(polygonPoints);
        } else {
            // 可能是凹多边形，使用最近邻边界追踪法
            System.out.println("多边形分析：检测为可能的凹多边形，使用最近邻边界追踪算法");
            return nearestNeighborBoundaryTracing(polygonPoints);
        }
    }

    /**
     * 对简单多边形（3-4个顶点）进行排序
     * 这是针对常见围栏场景的优化方法
     * 
     * @param polygonPoints 多边形顶点坐标列表
     * @return 排序后的顶点列表
     * @author Shawn
     * @date 2025/07/09
     */
    private static List<double[]> sortSimplePolygonVertices(List<double[]> polygonPoints) {
        // 对于简单的矩形或三角形，按角度排序通常是最有效的
        return sortVerticesByAngle(polygonPoints);
    }

    /**
     * 通用的按角度排序算法
     * 适用于凸多边形
     * 
     * @param polygonPoints 多边形顶点坐标列表
     * @return 排序后的顶点列表
     * @author Shawn
     * @date 2025/07/09
     */
    private static List<double[]> sortVerticesByAngle(List<double[]> polygonPoints) {
        // 计算中心点
        double centerX = 0, centerY = 0;
        for (double[] point : polygonPoints) {
            centerX += point[0];
            centerY += point[1];
        }
        centerX /= polygonPoints.size();
        centerY /= polygonPoints.size();

        // 创建包含角度信息的点列表
        List<VertexWithAngle> verticesWithAngle = new ArrayList<>();
        for (double[] point : polygonPoints) {
            double angle = Math.atan2(point[1] - centerY, point[0] - centerX);
            verticesWithAngle.add(new VertexWithAngle(point[0], point[1], angle));
        }

        // 按照角度排序（逆时针方向）
        Collections.sort(verticesWithAngle, new Comparator<VertexWithAngle>() {
            @Override
            public int compare(VertexWithAngle v1, VertexWithAngle v2) {
                return Double.compare(v1.angle, v2.angle);
            }
        });

        // 转换回double[]数组列表
        List<double[]> sortedPoints = new ArrayList<>();
        for (VertexWithAngle vertex : verticesWithAngle) {
            sortedPoints.add(new double[] { vertex.x, vertex.y });
        }

        return sortedPoints;
    }

    // ✅ 已实现凹多边形支持，使用最近邻边界追踪算法
    // 当前支持的算法：
    // 1. 角度排序法 (适用于凸多边形，性能优秀 O(n log n))
    // 2. 最近邻边界追踪法 (适用于凹多边形，兼容性好 O(n²))
    // 3. 智能选择机制 (自动选择最适合的算法)

    /**
     * 判断多边形是否可能是凸多边形
     * 通过检查边界框和点分布来快速判断
     * 
     * @param polygonPoints 多边形顶点坐标列表
     * @return 如果可能是凸多边形返回true
     * @author Shawn
     * @date 2025/07/09
     */
    private static boolean isLikelyConvexPolygon(List<double[]> polygonPoints) {
        if (polygonPoints.size() <= 4) {
            return true; // 简单情况，倾向于认为是凸多边形
        }

        // 计算边界框
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (double[] point : polygonPoints) {
            minX = Math.min(minX, point[0]);
            maxX = Math.max(maxX, point[0]);
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
        }

        // 检查点分布：如果大部分点都在边界附近，可能是凸多边形
        double threshold = 0.3; // 容忍度
        double width = maxX - minX;
        double height = maxY - minY;
        int boundaryPointCount = 0;

        for (double[] point : polygonPoints) {
            boolean nearBoundary = (point[0] - minX) < threshold * width ||
                    (maxX - point[0]) < threshold * width ||
                    (point[1] - minY) < threshold * height ||
                    (maxY - point[1]) < threshold * height;

            if (nearBoundary) {
                boundaryPointCount++;
            }
        }

        // 如果超过70%的点都在边界附近，可能是凸多边形
        return (double) boundaryPointCount / polygonPoints.size() > 0.7;
    }

    /**
     * 最近邻边界追踪法排序多边形顶点
     * 适用于凸多边形和凹多边形
     * 
     * @param polygonPoints 多边形顶点坐标列表
     * @return 排序后的顶点列表
     * @author Shawn
     * @date 2025/07/09
     */
    private static List<double[]> nearestNeighborBoundaryTracing(List<double[]> polygonPoints) {
        if (polygonPoints.size() < 3) {
            return new ArrayList<>(polygonPoints);
        }

        // 1. 找到起始点（最左下角的点）
        double[] startPoint = findBottomLeftPoint(polygonPoints);
        if (startPoint == null) {
            // 降级到角度排序算法
            return sortVerticesByAngle(polygonPoints);
        }

        // 2. 初始化追踪
        List<double[]> sortedPoints = new ArrayList<>();
        List<double[]> remainingPoints = new ArrayList<>(polygonPoints);

        sortedPoints.add(startPoint);
        remainingPoints.remove(startPoint);

        double[] currentPoint = startPoint;
        double[] previousDirection = new double[] { 1.0, 0.0 }; // 初始方向向右

        // 3. 边界追踪主循环（添加安全计数器防止无限循环）
        int maxIterations = polygonPoints.size() * 2; // 安全限制
        int iteration = 0;

        while (!remainingPoints.isEmpty() && iteration < maxIterations) {
            iteration++;

            double[] nextPoint = findNextBoundaryPoint(currentPoint, previousDirection, remainingPoints);

            if (nextPoint == null) {
                // 无法找到下一个点，使用最近点作为备选
                nextPoint = findNearestPoint(currentPoint, remainingPoints);
                if (nextPoint == null) {
                    // 仍然无法找到，可能数据有问题，跳出循环
                    break;
                }
            }

            sortedPoints.add(nextPoint);
            remainingPoints.remove(nextPoint);

            // 更新方向向量
            previousDirection = new double[] {
                    nextPoint[0] - currentPoint[0],
                    nextPoint[1] - currentPoint[1]
            };
            normalizeVector(previousDirection);

            currentPoint = nextPoint;
        }

        // 如果还有剩余点，添加到结果中（异常情况处理）
        if (!remainingPoints.isEmpty()) {
            System.out.println("警告：边界追踪未能处理所有点，剩余 " + remainingPoints.size() + " 个点");
            sortedPoints.addAll(remainingPoints);
        }

        return sortedPoints;
    }

    /**
     * 找到最左下角的点作为起始点
     * 返回原始列表中的引用，确保可以正确从列表中移除
     */
    private static double[] findBottomLeftPoint(List<double[]> points) {
        if (points.isEmpty()) {
            return null;
        }

        double[] bottomLeft = points.get(0);

        for (double[] point : points) {
            if (point[1] < bottomLeft[1] ||
                    (Math.abs(point[1] - bottomLeft[1]) < 1e-9 && point[0] < bottomLeft[0])) {
                bottomLeft = point;
            }
        }

        return bottomLeft;
    }

    /**
     * 找到下一个边界点
     */
    private static double[] findNextBoundaryPoint(double[] currentPoint, double[] direction,
            List<double[]> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        double[] bestCandidate = null;
        double bestAngle = Double.MAX_VALUE;

        for (double[] candidate : candidates) {
            // 计算当前点到候选点的向量
            double[] toCandidate = new double[] {
                    candidate[0] - currentPoint[0],
                    candidate[1] - currentPoint[1]
            };

            // 跳过重合点
            if (Math.abs(toCandidate[0]) < 1e-9 && Math.abs(toCandidate[1]) < 1e-9) {
                continue;
            }

            normalizeVector(toCandidate);

            // 计算与前进方向的夹角（使用叉积判断转向）
            double crossProduct = direction[0] * toCandidate[1] - direction[1] * toCandidate[0];
            double dotProduct = direction[0] * toCandidate[0] + direction[1] * toCandidate[1];

            // 计算角度，优先选择左转角度最小的点
            double angle = Math.atan2(crossProduct, dotProduct);
            if (angle < 0) {
                angle += 2 * Math.PI; // 转换为0-2π范围
            }

            if (angle < bestAngle) {
                bestAngle = angle;
                bestCandidate = candidate;
            }
        }

        return bestCandidate;
    }

    /**
     * 找到距离当前点最近的点（备用方法）
     */
    private static double[] findNearestPoint(double[] currentPoint, List<double[]> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        double[] nearest = candidates.get(0);
        double minDistance = calculateDistance(currentPoint, nearest);

        for (double[] candidate : candidates) {
            double distance = calculateDistance(currentPoint, candidate);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }

        return nearest;
    }

    /**
     * 计算两点之间的距离
     */
    private static double calculateDistance(double[] point1, double[] point2) {
        double dx = point1[0] - point2[0];
        double dy = point1[1] - point2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 向量归一化
     */
    private static void normalizeVector(double[] vector) {
        double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
        if (length > 1e-9) {
            vector[0] /= length;
            vector[1] /= length;
        }
    }

    /**
     * 带角度信息的顶点类
     */
    private static class VertexWithAngle {
        double x, y, angle;

        VertexWithAngle(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }

    /**
     * 格式化顶点坐标用于调试输出
     *
     * @param points 顶点坐标列表
     * @return 格式化后的字符串
     * @author Shawn
     * @date 2025/07/09
     */
    private static String formatPointsForDebug(List<double[]> points) {
        if (points == null || points.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            double[] point = points.get(i);
            sb.append(String.format("(%.2f, %.2f)", point[0], point[1]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 判断一个点是否在线段上
     * 
     * @param x  待判断点的X坐标
     * @param y  待判断点的Y坐标
     * @param x1 线段起点X坐标
     * @param y1 线段起点Y坐标
     * @param x2 线段终点X坐标
     * @param y2 线段终点Y坐标
     * @return 如果点在线段上返回true，否则返回false
     */
    private static boolean isPointOnLine(double x, double y, double x1, double y1, double x2, double y2) {
        // 判断点是否在线段的包围盒内
        if (x < Math.min(x1, x2) || x > Math.max(x1, x2) ||
                y < Math.min(y1, y2) || y > Math.max(y1, y2)) {
            return false;
        }

        // 如果线段是垂直的
        if (Math.abs(x1 - x2) < 0.000001) {
            return Math.abs(x - x1) < 0.000001;
        }

        // 如果线段是水平的
        if (Math.abs(y1 - y2) < 0.000001) {
            return Math.abs(y - y1) < 0.000001;
        }

        // 计算斜率并比较
        double slope = (y2 - y1) / (x2 - x1);
        double intercept = y1 - slope * x1;
        return Math.abs(y - (slope * x + intercept)) < 0.000001;
    }

    /**
     * 判断一个点是否在多边形内部（接收单独的坐标数组版本）
     * 
     * @param x        待判断点的X坐标
     * @param y        待判断点的Y坐标
     * @param polygonX 多边形顶点的X坐标数组
     * @param polygonY 多边形顶点的Y坐标数组
     * @return 如果点在多边形内部返回true，否则返回false
     */
    public static boolean isPointInPolygon(double x, double y, double[] polygonX, double[] polygonY) {
        if (polygonX == null || polygonY == null || polygonX.length != polygonY.length || polygonX.length < 3) {
            return false;
        }

        int n = polygonX.length;
        double[][] polygon = new double[n][2];
        for (int i = 0; i < n; i++) {
            polygon[i][0] = polygonX[i];
            polygon[i][1] = polygonY[i];
        }

        return isPointInPolygon(x, y, polygon);
    }

    /**
     * 测试方法：演示凹多边形和凸多边形的支持
     * 仅用于功能验证，生产环境中可以移除
     * 
     * @author Shawn
     * @date 2025/07/09
     */
    public static void testPolygonSupport() {
        System.out.println("=== GeometryUtils 多边形支持测试 ===");

        // 测试1：矩形（凸多边形）
        List<double[]> rectangle = new ArrayList<>();
        rectangle.add(new double[] { 7722, 2802 });
        rectangle.add(new double[] { 7808, 2801 });
        rectangle.add(new double[] { 7807, 3128 });
        rectangle.add(new double[] { 7722, 3128 });

        System.out.println("\n测试1：矩形围栏");
        List<double[]> sortedRectangle = sortPolygonVertices(rectangle);

        // 测试点
        double testX = 7764.831800000001;
        double testY = 2914.6766;
        boolean inRectangle = isPointInPolygon(testX, testY, sortedRectangle);
        System.out.println("测试点 (" + testX + ", " + testY + ") 是否在矩形内: " + inRectangle);

        // 测试2：L型多边形（凹多边形）
        List<double[]> lShape = new ArrayList<>();
        lShape.add(new double[] { 0, 0 });
        lShape.add(new double[] { 10, 0 });
        lShape.add(new double[] { 10, 5 });
        lShape.add(new double[] { 5, 5 });
        lShape.add(new double[] { 5, 10 });
        lShape.add(new double[] { 0, 10 });

        System.out.println("\n测试2：L型围栏");
        List<double[]> sortedLShape = sortPolygonVertices(lShape);

        // 测试点：凹陷部分内的点
        boolean inLShape1 = isPointInPolygon(7, 7, sortedLShape); // 应该在凹陷内，返回false
        boolean inLShape2 = isPointInPolygon(2, 2, sortedLShape); // 应该在主体内，返回true
        System.out.println("测试点 (7, 7) 是否在L型内: " + inLShape1 + " (应该为false，在凹陷部分)");
        System.out.println("测试点 (2, 2) 是否在L型内: " + inLShape2 + " (应该为true，在主体部分)");

        System.out.println("\n=== 测试完成 ===");
    }
}